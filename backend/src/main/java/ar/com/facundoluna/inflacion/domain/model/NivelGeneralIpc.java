package ar.com.facundoluna.inflacion.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad de dominio: el número índice del nivel general del IPC para un período,
 * región y tipo de serie determinados. Es el dato "crudo" tal como lo publica el
 * INDEC (ya filtrado a nivel general, división "Nivel General"), antes de calcular
 * ninguna variación.
 */
public final class NivelGeneralIpc {

    private final PeriodoMensual periodo;
    private final RegionIpc region;
    private final SerieTipo serie;
    private final BigDecimal indice;

    public NivelGeneralIpc(PeriodoMensual periodo, RegionIpc region, SerieTipo serie, BigDecimal indice) {
        this.periodo = Objects.requireNonNull(periodo, "periodo no puede ser null");
        this.region = Objects.requireNonNull(region, "region no puede ser null");
        this.serie = Objects.requireNonNull(serie, "serie no puede ser null");
        this.indice = Objects.requireNonNull(indice, "indice no puede ser null");
        if (indice.signum() < 0) {
            throw new IllegalArgumentException("El índice IPC no puede ser negativo: " + indice);
        }
    }

    public PeriodoMensual periodo() {
        return periodo;
    }

    public RegionIpc region() {
        return region;
    }

    public SerieTipo serie() {
        return serie;
    }

    public BigDecimal indice() {
        return indice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NivelGeneralIpc that)) return false;
        return periodo.equals(that.periodo) && region == that.region && serie == that.serie;
    }

    @Override
    public int hashCode() {
        return Objects.hash(periodo, region, serie);
    }

    @Override
    public String toString() {
        return "NivelGeneralIpc[periodo=%s, region=%s, serie=%s, indice=%s]"
                .formatted(periodo, region, serie, indice);
    }
}
