package ar.com.facundoluna.inflacion.application.usecase;

import ar.com.facundoluna.inflacion.application.port.out.IndicadorIpcRepositoryPort;
import ar.com.facundoluna.inflacion.domain.model.EvolucionSueldoReal;
import ar.com.facundoluna.inflacion.domain.model.PeriodoMensual;
import ar.com.facundoluna.inflacion.domain.model.RegionIpc;
import ar.com.facundoluna.inflacion.domain.model.SerieTipo;
import ar.com.facundoluna.inflacion.domain.service.CalculadoraPoderCompra;

import java.math.BigDecimal;
import java.util.List;

/**
 * Caso de uso: simular la evolución del poder de compra de un sueldo nominal fijo
 * desde un período inicial hasta el último dato disponible.
 */
public class CalcularPoderCompraUseCase {

    private final IndicadorIpcRepositoryPort repositorio;
    private final CalculadoraPoderCompra calculadora;

    public CalcularPoderCompraUseCase(IndicadorIpcRepositoryPort repositorio, CalculadoraPoderCompra calculadora) {
        this.repositorio = repositorio;
        this.calculadora = calculadora;
    }

    public List<EvolucionSueldoReal> ejecutar(PeriodoMensual periodoInicial, BigDecimal sueldoNominal) {
        PeriodoMensual ultimo = repositorio.ultimoPeriodoCargado()
                .orElseThrow(() -> new IllegalStateException(
                        "Todavía no hay datos de IPC sincronizados. Ejecutá POST /api/v1/ipc/sync primero."));

        var serieIndices = repositorio.buscarSerie(
                RegionIpc.NACIONAL, SerieTipo.ORIGINAL, periodoInicial, ultimo);

        return calculadora.calcular(serieIndices, periodoInicial, sueldoNominal);
    }
}
