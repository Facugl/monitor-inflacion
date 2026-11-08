package ar.com.facundoluna.inflacion.domain.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Fórmula de variación porcentual entre dos números índice, compartida por las
 * calculadoras de dominio (nivel general y categorías).
 *
 * <p>Existe para que la aritmética del INDEC —incluida la escala y el modo de
 * redondeo— esté definida en UN solo lugar: si el nivel general y las categorías
 * redondearan distinto, los números del dashboard no cerrarían entre sí.</p>
 */
public final class VariacionPorcentual {

    /** El INDEC publica las variaciones con dos decimales. */
    private static final int ESCALA_DECIMAL = 2;
    private static final MathContext CONTEXTO = new MathContext(10, RoundingMode.HALF_UP);

    private VariacionPorcentual() {
        // clase de utilidad
    }

    /**
     * Variación porcentual de {@code valorActual} respecto de {@code valorBase},
     * es decir {@code (actual / base - 1) * 100}.
     *
     * @return {@code null} si falta alguno de los dos valores o si la base es cero
     *         (no hay variación definible), nunca una excepción: que falte historia
     *         para calcular un período es un caso esperado, no un error.
     */
    public static BigDecimal entre(BigDecimal valorActual, BigDecimal valorBase) {
        if (valorActual == null || valorBase == null || valorBase.signum() == 0) {
            return null;
        }
        BigDecimal razon = valorActual.divide(valorBase, CONTEXTO);
        return razon.subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100))
                .setScale(ESCALA_DECIMAL, RoundingMode.HALF_UP);
    }
}
