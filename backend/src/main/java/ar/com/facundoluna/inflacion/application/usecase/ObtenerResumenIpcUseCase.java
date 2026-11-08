package ar.com.facundoluna.inflacion.application.usecase;

import ar.com.facundoluna.inflacion.application.port.out.IndicadorIpcRepositoryPort;
import ar.com.facundoluna.inflacion.domain.model.PeriodoMensual;
import ar.com.facundoluna.inflacion.domain.model.RegionIpc;
import ar.com.facundoluna.inflacion.domain.model.SerieTipo;
import ar.com.facundoluna.inflacion.domain.model.VariacionIpc;
import ar.com.facundoluna.inflacion.domain.service.CalculadoraVariacionesIpc;

import java.util.NoSuchElementException;

/**
 * Caso de uso: obtener el "dato del mes" — la variación del último período
 * cargado, el que se muestra como titular del dashboard.
 */
public class ObtenerResumenIpcUseCase {

    private final IndicadorIpcRepositoryPort repositorio;
    private final CalculadoraVariacionesIpc calculadora;

    public ObtenerResumenIpcUseCase(IndicadorIpcRepositoryPort repositorio, CalculadoraVariacionesIpc calculadora) {
        this.repositorio = repositorio;
        this.calculadora = calculadora;
    }

    public VariacionIpc ejecutar(RegionIpc region, SerieTipo serie) {
        PeriodoMensual ultimo = repositorio.ultimoPeriodoCargado()
                .orElseThrow(() -> new NoSuchElementException(
                        "Todavía no hay datos de IPC sincronizados. Ejecutá POST /api/v1/ipc/sync primero."));

        PeriodoMensual desde = PeriodoMensual.of(ultimo.anio() - 1, 1);
        var serieIndices = repositorio.buscarSerie(region, serie, desde, ultimo);
        var variaciones = calculadora.calcular(serieIndices);

        return calculadora.buscarPeriodo(variaciones, ultimo)
                .orElseThrow(() -> new NoSuchElementException(
                        "No se encontró el período " + ultimo + " para region=" + region + " serie=" + serie));
    }
}
