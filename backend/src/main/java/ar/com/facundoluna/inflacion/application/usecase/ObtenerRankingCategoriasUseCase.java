package ar.com.facundoluna.inflacion.application.usecase;

import ar.com.facundoluna.inflacion.application.port.out.IndiceCategoriaRepositoryPort;
import ar.com.facundoluna.inflacion.domain.model.PeriodoMensual;
import ar.com.facundoluna.inflacion.domain.model.RegionIpc;
import ar.com.facundoluna.inflacion.domain.model.VariacionCategoriaIpc;
import ar.com.facundoluna.inflacion.domain.service.CalculadoraVariacionesPorCategoria;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Caso de uso: "¿qué aumentó más este mes?". Devuelve las categorías del IPC
 * ordenadas por variación mensual, de mayor a menor.
 */
public class ObtenerRankingCategoriasUseCase {

    private final IndiceCategoriaRepositoryPort repositorio;
    private final CalculadoraVariacionesPorCategoria calculadora;

    public ObtenerRankingCategoriasUseCase(
            IndiceCategoriaRepositoryPort repositorio, CalculadoraVariacionesPorCategoria calculadora) {
        this.repositorio = repositorio;
        this.calculadora = calculadora;
    }

    /**
     * @param periodo período a rankear, o {@code null} para usar el último cargado.
     */
    public List<VariacionCategoriaIpc> ejecutar(RegionIpc region, PeriodoMensual periodo) {
        PeriodoMensual objetivo = periodo != null ? periodo : ultimoCargado();

        // Desde enero del año anterior: cubre tanto diciembre previo (para la acumulada
        // del año) como el mismo mes del año anterior (para la interanual).
        PeriodoMensual desde = PeriodoMensual.of(objetivo.anio() - 1, 1);

        var serie = repositorio.buscarSerie(region, desde, objetivo);
        var ranking = calculadora.rankingDelPeriodo(serie, objetivo);

        if (ranking.isEmpty()) {
            throw new NoSuchElementException(
                    "No hay datos de categorías para el período " + objetivo.codigoIndec()
                            + " en la región " + region + ". Ejecutá POST /api/v1/ipc/sync primero.");
        }
        return ranking;
    }

    private PeriodoMensual ultimoCargado() {
        return repositorio.ultimoPeriodoCargado()
                .orElseThrow(() -> new NoSuchElementException(
                        "Todavía no hay datos de categorías sincronizados. "
                                + "Ejecutá POST /api/v1/ipc/sync primero."));
    }
}
