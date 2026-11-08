package ar.com.facundoluna.inflacion.application.usecase;

import ar.com.facundoluna.inflacion.application.port.out.IndecApiClientPort;
import ar.com.facundoluna.inflacion.application.port.out.IndicadorIpcRepositoryPort;
import ar.com.facundoluna.inflacion.domain.model.NivelGeneralIpc;
import ar.com.facundoluna.inflacion.domain.model.PeriodoMensual;

import java.util.List;

/**
 * Caso de uso: sincronizar los datos del nivel general del IPC desde la API-INDEC
 * hacia la base de datos local. Es idempotente (upsert) y es seguro ejecutarlo
 * repetidas veces, manual o periódicamente.
 */
public class SincronizarIpcUseCase {

    /** Primer período con datos en el endpoint ipc_nacional de la API-INDEC. */
    private static final PeriodoMensual PERIODO_INICIO_SERIE_INDEC = PeriodoMensual.of(2016, 12);

    private final IndecApiClientPort indecApiClient;
    private final IndicadorIpcRepositoryPort repositorio;

    public SincronizarIpcUseCase(IndecApiClientPort indecApiClient, IndicadorIpcRepositoryPort repositorio) {
        this.indecApiClient = indecApiClient;
        this.repositorio = repositorio;
    }

    /**
     * @return cantidad de puntos de la serie sincronizados en esta corrida.
     */
    public int ejecutar() {
        PeriodoMensual desde = repositorio.ultimoPeriodoCargado()
                .map(PeriodoMensual::mesAnterior) // re-trae el último mes por si INDEC lo revisó
                .orElse(PERIODO_INICIO_SERIE_INDEC);

        List<NivelGeneralIpc> datos = indecApiClient.obtenerNivelGeneralIpcNacional(desde);
        repositorio.guardarTodos(datos);
        return datos.size();
    }
}
