package ar.com.facundoluna.inflacion.domain;

import ar.com.facundoluna.inflacion.domain.model.NivelGeneralIpc;
import ar.com.facundoluna.inflacion.domain.model.PeriodoMensual;
import ar.com.facundoluna.inflacion.domain.model.RegionIpc;
import ar.com.facundoluna.inflacion.domain.model.SerieTipo;
import ar.com.facundoluna.inflacion.domain.service.CalculadoraVariacionesIpc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraVariacionesIpcTest {

    private final CalculadoraVariacionesIpc calculadora = new CalculadoraVariacionesIpc();

    private NivelGeneralIpc dato(int anio, int mes, String indice) {
        return new NivelGeneralIpc(
                PeriodoMensual.of(anio, mes), RegionIpc.NACIONAL, SerieTipo.ORIGINAL, new BigDecimal(indice));
    }

    @Nested
    @DisplayName("Variación mensual")
    class VariacionMensual {

        @Test
        @DisplayName("se calcula contra el índice del mes inmediatamente anterior")
        void calculaVariacionMensual() {
            var serie = List.of(dato(2026, 9, "100.00"), dato(2026, 10, "102.50"));

            var resultado = calculadora.calcular(serie);

            var octubre = resultado.get(1);
            assertThat(octubre.variacionMensualPct()).isEqualByComparingTo("2.50");
        }

        @Test
        @DisplayName("es null cuando no hay dato del mes anterior")
        void esNullSinMesAnterior() {
            var serie = List.of(dato(2026, 9, "100.00"));

            var resultado = calculadora.calcular(serie);

            assertThat(resultado.get(0).variacionMensualPct()).isNull();
        }
    }

    @Nested
    @DisplayName("Variación interanual")
    class VariacionInteranual {

        @Test
        @DisplayName("se calcula contra el mismo mes del año anterior")
        void calculaVariacionInteranual() {
            var serie = List.of(dato(2025, 10, "80.00"), dato(2026, 10, "100.00"));

            var resultado = calculadora.calcular(serie);

            var octubre2026 = resultado.get(1);
            assertThat(octubre2026.variacionInteranualPct()).isEqualByComparingTo("25.00");
        }
    }

    @Nested
    @DisplayName("Variación acumulada en el año")
    class VariacionAcumulada {

        @Test
        @DisplayName("se calcula contra diciembre del año anterior")
        void calculaVariacionAcumulada() {
            var serie = List.of(
                    dato(2025, 12, "100.00"),
                    dato(2026, 1, "102.00"),
                    dato(2026, 2, "105.06"));

            var resultado = calculadora.calcular(serie);

            var febrero = resultado.get(2);
            assertThat(febrero.variacionAcumuladaAnioPct()).isEqualByComparingTo("5.06");
        }

        @Test
        @DisplayName("sin diciembre anterior cargado, es null")
        void esNullSinDiciembreAnterior() {
            var serie = List.of(dato(2026, 2, "105.06"));

            var resultado = calculadora.calcular(serie);

            assertThat(resultado.get(0).variacionAcumuladaAnioPct()).isNull();
        }
    }

    @Test
    @DisplayName("el orden de entrada no importa: el resultado queda ordenado cronológicamente")
    void ordenaCronologicamenteSinImportarElOrdenDeEntrada() {
        var serieDesordenada = List.of(dato(2026, 3, "110"), dato(2026, 1, "100"), dato(2026, 2, "105"));

        var resultado = calculadora.calcular(serieDesordenada);

        assertThat(resultado).extracting(v -> v.periodo().mes()).containsExactly(1, 2, 3);
    }
}
