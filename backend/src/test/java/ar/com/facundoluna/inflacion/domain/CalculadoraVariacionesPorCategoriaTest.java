package ar.com.facundoluna.inflacion.domain;

import ar.com.facundoluna.inflacion.domain.model.CategoriaIpc;
import ar.com.facundoluna.inflacion.domain.model.IndiceCategoriaIpc;
import ar.com.facundoluna.inflacion.domain.model.PeriodoMensual;
import ar.com.facundoluna.inflacion.domain.model.RegionIpc;
import ar.com.facundoluna.inflacion.domain.model.VariacionCategoriaIpc;
import ar.com.facundoluna.inflacion.domain.service.CalculadoraVariacionesPorCategoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraVariacionesPorCategoriaTest {

    private final CalculadoraVariacionesPorCategoria calculadora = new CalculadoraVariacionesPorCategoria();

    private static final CategoriaIpc ALIMENTOS = new CategoriaIpc("01", "Alimentos y bebidas no alcohólicas");
    private static final CategoriaIpc SALUD = new CategoriaIpc("06", "Salud");
    private static final CategoriaIpc TRANSPORTE = new CategoriaIpc("07", "Transporte");

    private static IndiceCategoriaIpc dato(CategoriaIpc categoria, int anio, int mes, String indice) {
        return new IndiceCategoriaIpc(
                PeriodoMensual.of(anio, mes), RegionIpc.NACIONAL, categoria, new BigDecimal(indice));
    }

    @Nested
    @DisplayName("Orden del ranking")
    class Orden {

        @Test
        void ordenaDeMayorAMenorVariacionMensual() {
            var serie = List.of(
                    dato(ALIMENTOS, 2026, 8, "100.00"),
                    dato(ALIMENTOS, 2026, 9, "103.00"),   // +3,00%
                    dato(SALUD, 2026, 8, "100.00"),
                    dato(SALUD, 2026, 9, "101.00"),       // +1,00%
                    dato(TRANSPORTE, 2026, 8, "100.00"),
                    dato(TRANSPORTE, 2026, 9, "105.00")   // +5,00%
            );

            var ranking = calculadora.rankingDelPeriodo(serie, PeriodoMensual.of(2026, 9));

            assertThat(ranking).extracting(v -> v.categoria().codigo())
                    .containsExactly("07", "01", "06");
            assertThat(ranking).extracting(VariacionCategoriaIpc::variacionMensualPct)
                    .containsExactly(
                            new BigDecimal("5.00"), new BigDecimal("3.00"), new BigDecimal("1.00"));
        }

        @Test
        void mandaAlFinalLasCategoriasSinMesAnteriorParaComparar() {
            var serie = List.of(
                    dato(ALIMENTOS, 2026, 9, "103.00"),   // sin agosto: no hay mensual
                    dato(SALUD, 2026, 8, "100.00"),
                    dato(SALUD, 2026, 9, "101.00")        // +1,00%
            );

            var ranking = calculadora.rankingDelPeriodo(serie, PeriodoMensual.of(2026, 9));

            assertThat(ranking).extracting(v -> v.categoria().codigo()).containsExactly("06", "01");
            assertThat(ranking.get(1).variacionMensualPct()).isNull();
        }

        @Test
        void desempataPorCodigoCoicopCuandoDosCategoriasVarianIgual() {
            var serie = List.of(
                    dato(TRANSPORTE, 2026, 8, "100.00"),
                    dato(TRANSPORTE, 2026, 9, "102.00"),
                    dato(ALIMENTOS, 2026, 8, "100.00"),
                    dato(ALIMENTOS, 2026, 9, "102.00")
            );

            var ranking = calculadora.rankingDelPeriodo(serie, PeriodoMensual.of(2026, 9));

            // misma variación: gana el código más bajo, y el orden es estable entre llamadas
            assertThat(ranking).extracting(v -> v.categoria().codigo()).containsExactly("01", "07");
        }
    }

    @Nested
    @DisplayName("Cálculo de variaciones")
    class Variaciones {

        @Test
        void calculaMensualInteranualYAcumuladaConLaMismaFormulaQueElNivelGeneral() {
            var serie = List.of(
                    dato(ALIMENTOS, 2025, 9, "100.00"),   // base interanual
                    dato(ALIMENTOS, 2025, 12, "110.00"),  // base acumulada del año
                    dato(ALIMENTOS, 2026, 8, "120.00"),   // base mensual
                    dato(ALIMENTOS, 2026, 9, "132.00")
            );

            var ranking = calculadora.rankingDelPeriodo(serie, PeriodoMensual.of(2026, 9));

            assertThat(ranking).hasSize(1);
            var alimentos = ranking.get(0);
            assertThat(alimentos.variacionMensualPct()).isEqualByComparingTo("10.00");    // 132/120
            assertThat(alimentos.variacionInteranualPct()).isEqualByComparingTo("32.00"); // 132/100
            assertThat(alimentos.variacionAcumuladaAnioPct()).isEqualByComparingTo("20.00"); // 132/110
        }

        @Test
        void dejaEnNullLasVariacionesQueNoSePuedenCalcularPorFaltaDeHistoria() {
            var serie = List.of(
                    dato(SALUD, 2026, 8, "100.00"),
                    dato(SALUD, 2026, 9, "102.00")
            );

            var ranking = calculadora.rankingDelPeriodo(serie, PeriodoMensual.of(2026, 9));

            var salud = ranking.get(0);
            assertThat(salud.variacionMensualPct()).isEqualByComparingTo("2.00");
            assertThat(salud.variacionInteranualPct()).isNull();
            assertThat(salud.variacionAcumuladaAnioPct()).isNull();
        }
    }

    @Nested
    @DisplayName("Casos borde")
    class CasosBorde {

        @Test
        void devuelveVacioSiNingunaCategoriaTieneDatoEnElPeriodoPedido() {
            var serie = List.of(dato(ALIMENTOS, 2026, 8, "100.00"));

            var ranking = calculadora.rankingDelPeriodo(serie, PeriodoMensual.of(2026, 9));

            assertThat(ranking).isEmpty();
        }

        @Test
        void devuelveVacioConSerieVacia() {
            assertThat(calculadora.rankingDelPeriodo(List.of(), PeriodoMensual.of(2026, 9))).isEmpty();
        }

        @Test
        void noLeAfectaElOrdenDeLaSerieDeEntrada() {
            var ordenada = List.of(
                    dato(ALIMENTOS, 2026, 8, "100.00"),
                    dato(ALIMENTOS, 2026, 9, "103.00"),
                    dato(SALUD, 2026, 8, "100.00"),
                    dato(SALUD, 2026, 9, "101.00"));

            var desordenada = new ArrayList<>(ordenada);
            java.util.Collections.reverse(desordenada);

            var periodo = PeriodoMensual.of(2026, 9);
            assertThat(calculadora.rankingDelPeriodo(desordenada, periodo))
                    .isEqualTo(calculadora.rankingDelPeriodo(ordenada, periodo));
        }

        @Test
        void ignoraLaCategoriaCuyaBaseEsCeroEnVezDeDividirPorCero() {
            var serie = List.of(
                    dato(ALIMENTOS, 2026, 8, "0.00"),
                    dato(ALIMENTOS, 2026, 9, "103.00"));

            var ranking = calculadora.rankingDelPeriodo(serie, PeriodoMensual.of(2026, 9));

            assertThat(ranking).hasSize(1);
            assertThat(ranking.get(0).variacionMensualPct()).isNull();
        }
    }
}
