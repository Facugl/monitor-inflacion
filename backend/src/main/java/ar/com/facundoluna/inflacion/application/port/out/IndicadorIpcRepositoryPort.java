package ar.com.facundoluna.inflacion.application.port.out;

import ar.com.facundoluna.inflacion.domain.model.NivelGeneralIpc;
import ar.com.facundoluna.inflacion.domain.model.PeriodoMensual;
import ar.com.facundoluna.inflacion.domain.model.RegionIpc;
import ar.com.facundoluna.inflacion.domain.model.SerieTipo;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (hexágono) hacia la persistencia de los índices del nivel
 * general del IPC. La capa de aplicación depende de esta interfaz, nunca de JPA.
 */
public interface IndicadorIpcRepositoryPort {

    void guardarTodos(List<NivelGeneralIpc> datos);

    List<NivelGeneralIpc> buscarSerie(RegionIpc region, SerieTipo serie, PeriodoMensual desde, PeriodoMensual hasta);

    Optional<NivelGeneralIpc> buscarUltimo(RegionIpc region, SerieTipo serie);

    Optional<PeriodoMensual> ultimoPeriodoCargado();
}
