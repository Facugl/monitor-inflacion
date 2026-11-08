package ar.com.facundoluna.inflacion.domain.service;

import ar.com.facundoluna.inflacion.domain.model.NivelGeneralIpc;
import ar.com.facundoluna.inflacion.domain.model.PeriodoMensual;
import ar.com.facundoluna.inflacion.domain.model.VariacionIpc;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio de dominio puro (sin Spring, sin persistencia) que calcula las variaciones
 * porcentuales del IPC a partir de una serie de índices del nivel general.
 *
 * <p>Las fórmulas siguen la metodología del INDEC:</p>
 * <ul>
 *   <li>Variación mensual = (índice_mes / índice_mes_anterior - 1) * 100</li>
 *   <li>Variación interanual = (índice_mes / índice_mismo_mes_año_anterior - 1) * 100</li>
 *   <li>Variación acumulada en el año = (índice_mes / índice_diciembre_año_anterior - 1) * 100</li>
 * </ul>
 */
public class CalculadoraVariacionesIpc {

    /**
     * Calcula las tres variaciones para cada punto de la serie recibida.
     *
     * @param serieOrdenada índices del nivel general, de la MISMA región y tipo de serie,
     *                       en cualquier orden (el método los ordena internamente por período).
     * @return lista de variaciones, en el mismo orden cronológico de la serie de entrada.
     */
    public List<VariacionIpc> calcular(List<NivelGeneralIpc> serieOrdenada) {
        Map<PeriodoMensual, NivelGeneralIpc> porPeriodo = new LinkedHashMap<>();
        serieOrdenada.stream()
                .sorted((a, b) -> a.periodo().compareTo(b.periodo()))
                .forEach(dato -> porPeriodo.put(dato.periodo(), dato));

        return porPeriodo.values().stream()
                .map(dato -> calcularUno(dato, porPeriodo))
                .toList();
    }

    private VariacionIpc calcularUno(NivelGeneralIpc actual, Map<PeriodoMensual, NivelGeneralIpc> porPeriodo) {
        BigDecimal variacionMensual = variacionContra(actual, porPeriodo.get(actual.periodo().mesAnterior()));
        BigDecimal variacionInteranual = variacionContra(actual, porPeriodo.get(actual.periodo().mismoMesAnioAnterior()));
        BigDecimal variacionAcumulada = calcularAcumuladaAnio(actual, porPeriodo);

        return new VariacionIpc(
                actual.periodo(),
                actual.region(),
                actual.serie(),
                actual.indice(),
                variacionMensual,
                variacionInteranual,
                variacionAcumulada
        );
    }

    private BigDecimal variacionContra(NivelGeneralIpc actual, NivelGeneralIpc base) {
        return VariacionPorcentual.entre(actual.indice(), base == null ? null : base.indice());
    }

    /**
     * La variación acumulada en el año es, por definición, la variación entre el índice
     * del mes y el índice de diciembre del año anterior (o, si el período ES enero,
     * coincide con la variación mensual).
     */
    private BigDecimal calcularAcumuladaAnio(NivelGeneralIpc actual, Map<PeriodoMensual, NivelGeneralIpc> porPeriodo) {
        NivelGeneralIpc diciembreAnterior = porPeriodo.get(actual.periodo().diciembreAnioAnterior());
        return variacionContra(actual, diciembreAnterior);
    }

    /**
     * Busca la variación de un período puntual dentro de una serie ya calculada.
     */
    public Optional<VariacionIpc> buscarPeriodo(List<VariacionIpc> serie, PeriodoMensual periodo) {
        return serie.stream().filter(v -> v.periodo().equals(periodo)).findFirst();
    }
}
