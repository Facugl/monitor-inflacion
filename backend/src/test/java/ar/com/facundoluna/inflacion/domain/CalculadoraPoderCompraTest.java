package ar.com.facundoluna.inflacion.domain;

import ar.com.facundoluna.inflacion.domain.model.NivelGeneralIpc;
import ar.com.facundoluna.inflacion.domain.model.PeriodoMensual;
import ar.com.facundoluna.inflacion.domain.model.RegionIpc;
import ar.com.facundoluna.inflacion.domain.model.SerieTipo;
import ar.com.facundoluna.inflacion.domain.service.CalculadoraPoderCompra;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculadoraPoderCompraTest {

    private final CalculadoraPoderCompra calculadora = new CalculadoraPoderCompra();

    private NivelGeneralIpc dato(int anio, int mes, String indice) {
        return new NivelGeneralIpc(
                PeriodoMensual.of(anio, mes), RegionIpc.NACIONAL, SerieTipo.ORIGINAL, new BigDecimal(indice));
    }

    @Test
    void sueldoFijoPierdePoderDeCompraSiElIndiceSube() {
        var serie = List.of(
                dato(2026, 1, "100.00"),
                dato(2026, 2, "110.00")); // 10% de inflación en el mes

        var resultado = calculadora.calcular(serie, PeriodoMensual.of(2026, 1), new BigDecimal("100000"));

        assertThat(resultado).hasSize(2);
        var febrero = resultado.get(1);
        // A precios de enero, 100.000 nominales en febrero representan 100000 / 1.10
        assertThat(febrero.sueldoAjustadoAPrecioInicial()).isEqualByComparingTo("90909.09");
        assertThat(febrero.perdidaOGananciaPoderCompraPct()).isEqualByComparingTo("-9.09");
    }

    @Test
    void elPrimerPuntoNoTienePerdidaPorDefinicion() {
        var serie = List.of(dato(2026, 1, "100.00"));

        var resultado = calculadora.calcular(serie, PeriodoMensual.of(2026, 1), new BigDecimal("100000"));

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).perdidaOGananciaPoderCompraPct()).isEqualByComparingTo("0.00");
    }

    @Test
    void fallaSiNoHayDatoParaElPeriodoInicial() {
        var serie = List.of(dato(2026, 2, "110.00"));

        assertThatThrownBy(() ->
                calculadora.calcular(serie, PeriodoMensual.of(2026, 1), new BigDecimal("100000")))
                .isInstanceOf(NoSuchElementException.class);
    }
}
