package ar.com.facundoluna.inflacion.application.usecase;

import ar.com.facundoluna.inflacion.application.port.out.IndicadorIpcRepositoryPort;
import ar.com.facundoluna.inflacion.domain.model.PeriodoMensual;
import ar.com.facundoluna.inflacion.domain.model.RegionIpc;
import ar.com.facundoluna.inflacion.domain.model.SerieTipo;
import ar.com.facundoluna.inflacion.domain.model.VariacionIpc;
import ar.com.facundoluna.inflacion.domain.service.CalculadoraVariacionesIpc;

import java.util.List;

/**
 * Caso de uso: obtener la evolución del IPC (índice + variaciones mensual, interanual
 * y acumulada) para una región y rango de períodos determinados.
 */
public class ObtenerEvolucionIpcUseCase {

    private final IndicadorIpcRepositoryPort repositorio;
    private final CalculadoraVariacionesIpc calculadora;

    public ObtenerEvolucionIpcUseCase(IndicadorIpcRepositoryPort repositorio, CalculadoraVariacionesIpc calculadora) {
        this.repositorio = repositorio;
        this.calculadora = calculadora;
    }

    public List<VariacionIpc> ejecutar(RegionIpc region, SerieTipo serie, PeriodoMensual desde, PeriodoMensual hasta) {
        // Pedimos un año calendario extra hacia atrás para poder calcular interanual
        // y acumulada del primer período solicitado.
        PeriodoMensual desdeConMargen = PeriodoMensual.of(desde.anio() - 1, 1);

        var serieIndices = repositorio.buscarSerie(region, serie, desdeConMargen, hasta);
        var variaciones = calculadora.calcular(serieIndices);

        return variaciones.stream()
                .filter(v -> !v.periodo().isBefore(desde) && !v.periodo().isAfter(hasta))
                .toList();
    }
}
