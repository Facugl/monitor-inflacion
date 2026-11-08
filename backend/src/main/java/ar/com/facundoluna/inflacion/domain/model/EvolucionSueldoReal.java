package ar.com.facundoluna.inflacion.domain.model;

import java.math.BigDecimal;

/**
 * Punto de la evolución de un sueldo en términos reales: cuánto representaba, en
 * poder de compra del período inicial, el sueldo nominal informado en cada mes.
 */
public record EvolucionSueldoReal(
        PeriodoMensual periodo,
        BigDecimal sueldoNominal,
        BigDecimal sueldoAjustadoAPrecioInicial,
        BigDecimal perdidaOGananciaPoderCompraPct
) {
}
