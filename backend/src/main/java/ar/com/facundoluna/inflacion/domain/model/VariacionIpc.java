package ar.com.facundoluna.inflacion.domain.model;

import java.math.BigDecimal;

/**
 * Value object con las variaciones porcentuales calculadas para un período de IPC:
 * mensual (vs. mes anterior), interanual (vs. mismo mes del año anterior) y
 * acumulada en el año (vs. diciembre del año anterior).
 *
 * <p>Cualquiera de los tres campos puede venir {@code null} cuando no hay suficiente
 * historia para calcularlo (p. ej. interanual del primer período cargado).</p>
 */
public record VariacionIpc(
        PeriodoMensual periodo,
        RegionIpc region,
        SerieTipo serie,
        BigDecimal indice,
        BigDecimal variacionMensualPct,
        BigDecimal variacionInteranualPct,
        BigDecimal variacionAcumuladaAnioPct
) {
}
