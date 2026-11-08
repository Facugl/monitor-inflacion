package ar.com.facundoluna.inflacion.domain.model;

import java.math.BigDecimal;

/**
 * Value object con las variaciones porcentuales calculadas para UNA categoría del IPC
 * en un período. Es el análogo de {@link VariacionIpc} a nivel de división COICOP, y
 * es lo que alimenta el ranking de "qué aumentó más este mes".
 *
 * <p>Como en {@link VariacionIpc}, cualquiera de las variaciones puede venir
 * {@code null} cuando no hay suficiente historia cargada para calcularla.</p>
 */
public record VariacionCategoriaIpc(
        PeriodoMensual periodo,
        RegionIpc region,
        CategoriaIpc categoria,
        BigDecimal indice,
        BigDecimal variacionMensualPct,
        BigDecimal variacionInteranualPct,
        BigDecimal variacionAcumuladaAnioPct
) {
}
