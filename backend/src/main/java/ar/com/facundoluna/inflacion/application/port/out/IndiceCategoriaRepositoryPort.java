package ar.com.facundoluna.inflacion.application.port.out;

import ar.com.facundoluna.inflacion.domain.model.IndiceCategoriaIpc;
import ar.com.facundoluna.inflacion.domain.model.PeriodoMensual;
import ar.com.facundoluna.inflacion.domain.model.RegionIpc;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (hexágono) hacia la persistencia de los índices del IPC abiertos
 * por categoría (división COICOP). Separado de
 * {@link IndicadorIpcRepositoryPort} porque son dos agregados distintos, con
 * cardinalidad distinta: el nivel general es una fila por período, las categorías
 * son doce.
 */
public interface IndiceCategoriaRepositoryPort {

    void guardarTodos(List<IndiceCategoriaIpc> datos);

    /**
     * Serie de TODAS las categorías de una región en el rango pedido (inclusive).
     */
    List<IndiceCategoriaIpc> buscarSerie(RegionIpc region, PeriodoMensual desde, PeriodoMensual hasta);

    Optional<PeriodoMensual> ultimoPeriodoCargado();
}
