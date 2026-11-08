package ar.com.facundoluna.inflacion.domain.service;

import ar.com.facundoluna.inflacion.domain.model.EvolucionSueldoReal;
import ar.com.facundoluna.inflacion.domain.model.NivelGeneralIpc;
import ar.com.facundoluna.inflacion.domain.model.PeriodoMensual;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Servicio de dominio puro que responde la pregunta: "si en el período inicial
 * ganaba X y nunca me ajustaron el sueldo, ¿cuánto poder de compra perdí mes a mes?".
 *
 * <p>Asume un sueldo nominal constante desde el período inicial (el caso más común
 * y más ilustrativo); no modela aumentos intermedios.</p>
 */
public class CalculadoraPoderCompra {

    private static final MathContext CONTEXTO = new MathContext(10, RoundingMode.HALF_UP);

    /**
     * @param serieIndices    serie histórica del nivel general del IPC (una sola región/serie),
     *                        debe incluir al menos el período inicial y los períodos a evaluar.
     * @param periodoInicial  período en el que se percibió {@code sueldoNominal} por primera vez.
     * @param sueldoNominal   monto nominal, constante a partir de {@code periodoInicial}.
     * @return evolución mes a mes del poder de compra real de ese sueldo nominal fijo,
     *         expresado a precios del período inicial.
     */
    public List<EvolucionSueldoReal> calcular(
            List<NivelGeneralIpc> serieIndices,
            PeriodoMensual periodoInicial,
            BigDecimal sueldoNominal
    ) {
        Map<PeriodoMensual, NivelGeneralIpc> porPeriodo = serieIndices.stream()
                .collect(Collectors.toMap(NivelGeneralIpc::periodo, d -> d));

        NivelGeneralIpc base = porPeriodo.get(periodoInicial);
        if (base == null) {
            throw new NoSuchElementException(
                    "No hay índice IPC cargado para el período inicial " + periodoInicial);
        }

        return porPeriodo.values().stream()
                .filter(dato -> !dato.periodo().isBefore(periodoInicial))
                .sorted(Comparator.comparing(NivelGeneralIpc::periodo))
                .map(dato -> calcularPunto(dato, base, sueldoNominal))
                .toList();
    }

    private EvolucionSueldoReal calcularPunto(NivelGeneralIpc dato, NivelGeneralIpc base, BigDecimal sueldoNominal) {
        BigDecimal deflactor = dato.indice().divide(base.indice(), CONTEXTO);
        BigDecimal sueldoAjustado = sueldoNominal.divide(deflactor, CONTEXTO)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal perdidaPct = sueldoAjustado.subtract(sueldoNominal)
                .divide(sueldoNominal, CONTEXTO)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        return new EvolucionSueldoReal(dato.periodo(), sueldoNominal, sueldoAjustado, perdidaPct);
    }
}
