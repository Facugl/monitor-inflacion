package ar.com.facundoluna.inflacion.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad de dominio: el número índice del IPC de UNA categoría (división COICOP)
 * para un período y región. Es el equivalente de {@link NivelGeneralIpc} un nivel
 * más abajo en la clasificación.
 *
 * <p>No lleva {@link SerieTipo} porque el INDEC publica las divisiones únicamente en
 * la serie original: no hay versión desestacionalizada por división.</p>
 */
public record IndiceCategoriaIpc(
        PeriodoMensual periodo,
        RegionIpc region,
        CategoriaIpc categoria,
        BigDecimal indice
) {
    public IndiceCategoriaIpc {
        Objects.requireNonNull(periodo, "periodo no puede ser null");
        Objects.requireNonNull(region, "region no puede ser null");
        Objects.requireNonNull(categoria, "categoria no puede ser null");
        Objects.requireNonNull(indice, "indice no puede ser null");
        if (indice.signum() < 0) {
            throw new IllegalArgumentException("El índice IPC no puede ser negativo: " + indice);
        }
    }
}
