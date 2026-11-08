package ar.com.facundoluna.inflacion.domain.service;

import ar.com.facundoluna.inflacion.domain.model.CategoriaIpc;
import ar.com.facundoluna.inflacion.domain.model.IndiceCategoriaIpc;
import ar.com.facundoluna.inflacion.domain.model.PeriodoMensual;
import ar.com.facundoluna.inflacion.domain.model.VariacionCategoriaIpc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de dominio puro (sin Spring, sin persistencia) que calcula, para un período
 * dado, cuánto varió cada categoría del IPC y devuelve el ranking ordenado.
 *
 * <p>Usa exactamente las mismas fórmulas que {@link CalculadoraVariacionesIpc} —ambas
 * delegan en {@link VariacionPorcentual}— para que el ranking de categorías y el nivel
 * general sean comparables entre sí.</p>
 */
public class CalculadoraVariacionesPorCategoria {

    /**
     * Calcula las variaciones de cada categoría presente en la serie y devuelve el
     * ranking del período pedido, de mayor a menor variación mensual.
     *
     * @param serie  índices por categoría de la MISMA región, abarcando al menos desde
     *               diciembre del año anterior (para la acumulada) y el mismo mes del
     *               año anterior (para la interanual). El orden de entrada es irrelevante.
     * @param periodo período del que se quiere el ranking.
     * @return una fila por categoría con datos en {@code periodo}; lista vacía si no hay
     *         ninguna. Las categorías sin variación mensual calculable van al final, para
     *         que un mes recién cargado sin su mes previo no encabece el ranking.
     */
    public List<VariacionCategoriaIpc> rankingDelPeriodo(List<IndiceCategoriaIpc> serie, PeriodoMensual periodo) {
        Map<String, Map<PeriodoMensual, IndiceCategoriaIpc>> porCategoria = indexarPorCategoriaYPeriodo(serie);

        List<VariacionCategoriaIpc> ranking = new ArrayList<>();
        for (Map<PeriodoMensual, IndiceCategoriaIpc> porPeriodo : porCategoria.values()) {
            IndiceCategoriaIpc actual = porPeriodo.get(periodo);
            if (actual == null) {
                continue; // esta categoría no tiene dato en el período pedido
            }
            ranking.add(calcularUna(actual, porPeriodo));
        }

        ranking.sort(ordenDelRanking());
        return List.copyOf(ranking);
    }

    private Map<String, Map<PeriodoMensual, IndiceCategoriaIpc>> indexarPorCategoriaYPeriodo(
            List<IndiceCategoriaIpc> serie) {
        Map<String, Map<PeriodoMensual, IndiceCategoriaIpc>> indice = new HashMap<>();
        for (IndiceCategoriaIpc dato : serie) {
            indice.computeIfAbsent(dato.categoria().codigo(), k -> new HashMap<>())
                    .put(dato.periodo(), dato);
        }
        return indice;
    }

    private VariacionCategoriaIpc calcularUna(
            IndiceCategoriaIpc actual, Map<PeriodoMensual, IndiceCategoriaIpc> porPeriodo) {
        PeriodoMensual periodo = actual.periodo();

        return new VariacionCategoriaIpc(
                periodo,
                actual.region(),
                actual.categoria(),
                actual.indice(),
                variacionContra(actual, porPeriodo.get(periodo.mesAnterior())),
                variacionContra(actual, porPeriodo.get(periodo.mismoMesAnioAnterior())),
                variacionContra(actual, porPeriodo.get(periodo.diciembreAnioAnterior()))
        );
    }

    private BigDecimal variacionContra(IndiceCategoriaIpc actual, IndiceCategoriaIpc base) {
        return VariacionPorcentual.entre(actual.indice(), base == null ? null : base.indice());
    }

    /**
     * Mayor variación mensual primero; los {@code null} al final. Desempata por código
     * COICOP para que dos categorías que varían igual salgan siempre en el mismo orden
     * (si no, el ranking "bailaría" entre llamadas y la UI parpadearía).
     */
    private Comparator<VariacionCategoriaIpc> ordenDelRanking() {
        return Comparator
                .comparing(VariacionCategoriaIpc::variacionMensualPct,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(VariacionCategoriaIpc::categoria);
    }
}
