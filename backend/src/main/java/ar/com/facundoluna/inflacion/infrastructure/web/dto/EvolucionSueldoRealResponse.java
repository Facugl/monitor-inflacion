package ar.com.facundoluna.inflacion.infrastructure.web.dto;

import ar.com.facundoluna.inflacion.domain.model.EvolucionSueldoReal;

import java.math.BigDecimal;

public record EvolucionSueldoRealResponse(
        String periodo,
        BigDecimal sueldoNominal,
        BigDecimal sueldoAjustadoAPrecioInicial,
        BigDecimal perdidaOGananciaPoderCompraPct
) {
    public static EvolucionSueldoRealResponse desde(EvolucionSueldoReal e) {
        return new EvolucionSueldoRealResponse(
                e.periodo().codigoIndec(),
                e.sueldoNominal(),
                e.sueldoAjustadoAPrecioInicial(),
                e.perdidaOGananciaPoderCompraPct()
        );
    }
}
