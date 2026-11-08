package ar.com.facundoluna.inflacion.infrastructure.web;

import ar.com.facundoluna.inflacion.application.usecase.CalcularPoderCompraUseCase;
import ar.com.facundoluna.inflacion.domain.model.PeriodoMensual;
import ar.com.facundoluna.inflacion.infrastructure.web.dto.EvolucionSueldoRealResponse;
import ar.com.facundoluna.inflacion.infrastructure.web.dto.PoderCompraRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/poder-compra")
@Tag(name = "Poder de compra", description = "Simulador: qué pasa con un sueldo nominal fijo frente a la inflación")
public class PoderCompraController {

    private final CalcularPoderCompraUseCase calcularPoderCompraUseCase;

    public PoderCompraController(CalcularPoderCompraUseCase calcularPoderCompraUseCase) {
        this.calcularPoderCompraUseCase = calcularPoderCompraUseCase;
    }

    @PostMapping
    @Operation(summary = "Simula la pérdida/ganancia de poder de compra de un sueldo nominal fijo mes a mes")
    public ResponseEntity<List<EvolucionSueldoRealResponse>> simular(@Valid @RequestBody PoderCompraRequest request) {
        var evolucion = calcularPoderCompraUseCase.ejecutar(
                PeriodoMensual.desdeCodigoIndec(request.periodoInicial()),
                request.sueldoNominal());

        return ResponseEntity.ok(evolucion.stream().map(EvolucionSueldoRealResponse::desde).toList());
    }
}
