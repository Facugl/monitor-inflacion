package ar.com.facundoluna.inflacion.application.usecase;

import ar.com.facundoluna.inflacion.application.port.out.IndecApiClientPort;
import ar.com.facundoluna.inflacion.application.port.out.IndiceCategoriaRepositoryPort;
import ar.com.facundoluna.inflacion.application.port.out.IndicadorIpcRepositoryPort;
import ar.com.facundoluna.inflacion.domain.model.IndiceCategoriaIpc;
import ar.com.facundoluna.inflacion.domain.model.NivelGeneralIpc;
import ar.com.facundoluna.inflacion.domain.model.PeriodoMensual;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Caso de uso: sincronizar los datos del IPC desde la API-INDEC hacia la base de
 * datos local — tanto el nivel general como la apertura por categoría. Es idempotente
 * (upsert) y es seguro ejecutarlo repetidas veces, manual o periódicamente.
 *
 * <p>Cada serie avanza con su propio cursor: si el INDEC publica el nivel general
 * antes que las aperturas por división (pasa), la serie que ya está al día no se
 * vuelve a traer completa.</p>
 */
public class SincronizarIpcUseCase {

    /** Primer período con datos en el endpoint ipc_nacional de la API-INDEC. */
    private static final PeriodoMensual PERIODO_INICIO_SERIE_INDEC = PeriodoMensual.of(2016, 12);

    private final IndecApiClientPort indecApiClient;
    private final IndicadorIpcRepositoryPort repositorioNivelGeneral;
    private final IndiceCategoriaRepositoryPort repositorioCategorias;

    public SincronizarIpcUseCase(
            IndecApiClientPort indecApiClient,
            IndicadorIpcRepositoryPort repositorioNivelGeneral,
            IndiceCategoriaRepositoryPort repositorioCategorias) {
        this.indecApiClient = indecApiClient;
        this.repositorioNivelGeneral = repositorioNivelGeneral;
        this.repositorioCategorias = repositorioCategorias;
    }

    /**
     * @return cuántos puntos de cada serie se sincronizaron en esta corrida.
     */
    public ResultadoSincronizacion ejecutar() {
        List<NivelGeneralIpc> nivelGeneral = indecApiClient.obtenerNivelGeneralIpcNacional(
                cursorDesde(repositorioNivelGeneral.ultimoPeriodoCargado()));
        repositorioNivelGeneral.guardarTodos(nivelGeneral);

        List<IndiceCategoriaIpc> categorias = indecApiClient.obtenerIndicesPorCategoriaNacional(
                cursorDesde(repositorioCategorias.ultimoPeriodoCargado()));
        repositorioCategorias.guardarTodos(categorias);

        return new ResultadoSincronizacion(nivelGeneral.size(), categorias.size());
    }

    /**
     * Desde dónde pedirle datos al INDEC: el mes anterior al último cargado —se re-trae
     * ese mes por si el INDEC lo revisó— o el inicio de la serie si la base está vacía.
     */
    private PeriodoMensual cursorDesde(Optional<PeriodoMensual> ultimoCargado) {
        return ultimoCargado
                .map((Function<PeriodoMensual, PeriodoMensual>) PeriodoMensual::mesAnterior)
                .orElse(PERIODO_INICIO_SERIE_INDEC);
    }
}
