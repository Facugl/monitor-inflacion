package ar.com.facundoluna.inflacion.infrastructure.web;

import ar.com.facundoluna.inflacion.application.usecase.ObtenerEvolucionIpcUseCase;
import ar.com.facundoluna.inflacion.application.usecase.ObtenerRankingCategoriasUseCase;
import ar.com.facundoluna.inflacion.application.usecase.ObtenerResumenIpcUseCase;
import ar.com.facundoluna.inflacion.application.usecase.SincronizarIpcUseCase;
import ar.com.facundoluna.inflacion.domain.model.PeriodoMensual;
import ar.com.facundoluna.inflacion.domain.model.RegionIpc;
import ar.com.facundoluna.inflacion.domain.model.SerieTipo;
import ar.com.facundoluna.inflacion.infrastructure.web.dto.SincronizacionResponse;
import ar.com.facundoluna.inflacion.infrastructure.web.dto.VariacionCategoriaResponse;
import ar.com.facundoluna.inflacion.infrastructure.web.dto.VariacionIpcResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ipc")
@Tag(name = "IPC", description = "Índice de Precios al Consumidor: evolución, resumen y sincronización")
public class IpcController {

    private final SincronizarIpcUseCase sincronizarIpcUseCase;
    private final ObtenerEvolucionIpcUseCase obtenerEvolucionIpcUseCase;
    private final ObtenerResumenIpcUseCase obtenerResumenIpcUseCase;
    private final ObtenerRankingCategoriasUseCase obtenerRankingCategoriasUseCase;

    public IpcController(
            SincronizarIpcUseCase sincronizarIpcUseCase,
            ObtenerEvolucionIpcUseCase obtenerEvolucionIpcUseCase,
            ObtenerResumenIpcUseCase obtenerResumenIpcUseCase,
            ObtenerRankingCategoriasUseCase obtenerRankingCategoriasUseCase) {
        this.sincronizarIpcUseCase = sincronizarIpcUseCase;
        this.obtenerEvolucionIpcUseCase = obtenerEvolucionIpcUseCase;
        this.obtenerResumenIpcUseCase = obtenerResumenIpcUseCase;
        this.obtenerRankingCategoriasUseCase = obtenerRankingCategoriasUseCase;
    }

    @PostMapping("/sync")
    @Operation(summary = "Sincroniza el IPC (nivel general y categorías) desde la API-INDEC hacia la base local")
    public ResponseEntity<SincronizacionResponse> sincronizar() {
        return ResponseEntity.ok(SincronizacionResponse.desde(sincronizarIpcUseCase.ejecutar()));
    }

    @GetMapping("/evolucion")
    @Operation(summary = "Evolución del IPC con variación mensual, interanual y acumulada en un rango de períodos")
    public ResponseEntity<List<VariacionIpcResponse>> evolucion(
            @RequestParam String desde,
            @RequestParam String hasta,
            @RequestParam(defaultValue = "NACIONAL") RegionIpc region,
            @RequestParam(defaultValue = "ORIGINAL") SerieTipo serie
    ) {
        var variaciones = obtenerEvolucionIpcUseCase.ejecutar(
                region, serie, PeriodoMensual.desdeCodigoIndec(desde), PeriodoMensual.desdeCodigoIndec(hasta));

        return ResponseEntity.ok(variaciones.stream().map(VariacionIpcResponse::desde).toList());
    }

    @GetMapping("/resumen")
    @Operation(summary = "El 'dato del mes': variación del último período sincronizado")
    public ResponseEntity<VariacionIpcResponse> resumen(
            @RequestParam(defaultValue = "NACIONAL") RegionIpc region,
            @RequestParam(defaultValue = "ORIGINAL") SerieTipo serie
    ) {
        var variacion = obtenerResumenIpcUseCase.ejecutar(region, serie);
        return ResponseEntity.ok(VariacionIpcResponse.desde(variacion));
    }

    @GetMapping("/categorias")
    @Operation(summary = "Qué aumentó más: las 12 divisiones COICOP del IPC rankeadas por variación mensual")
    public ResponseEntity<List<VariacionCategoriaResponse>> categorias(
            @RequestParam(required = false)
            @Parameter(description = "Período AAAAMM a rankear. Si se omite, usa el último sincronizado.")
            String periodo,
            @RequestParam(defaultValue = "NACIONAL") RegionIpc region
    ) {
        PeriodoMensual objetivo = periodo == null || periodo.isBlank()
                ? null
                : PeriodoMensual.desdeCodigoIndec(periodo);

        var ranking = obtenerRankingCategoriasUseCase.ejecutar(region, objetivo);
        return ResponseEntity.ok(ranking.stream().map(VariacionCategoriaResponse::desde).toList());
    }
}
