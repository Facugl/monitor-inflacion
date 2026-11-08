package ar.com.facundoluna.inflacion.application.port.out;

import ar.com.facundoluna.inflacion.domain.model.NivelGeneralIpc;
import ar.com.facundoluna.inflacion.domain.model.PeriodoMensual;

import java.util.List;

/**
 * Puerto de salida hacia la API-INDEC. La implementación (adaptador) concreta vive en
 * infrastructure.indec y es la única parte del sistema que sabe que existe un JWT,
 * un header Authorization y el formato PostgREST de la API-INDEC.
 */
public interface IndecApiClientPort {

    /**
     * Trae el nivel general del IPC nacional (serie original) para todos los
     * períodos disponibles desde {@code desde} en adelante.
     */
    List<NivelGeneralIpc> obtenerNivelGeneralIpcNacional(PeriodoMensual desde);
}
