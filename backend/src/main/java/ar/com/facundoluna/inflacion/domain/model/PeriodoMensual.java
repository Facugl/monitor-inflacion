package ar.com.facundoluna.inflacion.domain.model;

import java.time.YearMonth;
import java.util.Objects;

/**
 * Value object que representa un período mensual (año-mes) de referencia de un dato
 * publicado por el INDEC. Equivalente al formato AAAAMM que usa la API-INDEC, pero
 * con comportamiento de dominio (mes anterior, mismo mes del año anterior, etc.).
 */
public final class PeriodoMensual implements Comparable<PeriodoMensual> {

    private final YearMonth yearMonth;

    private PeriodoMensual(YearMonth yearMonth) {
        this.yearMonth = Objects.requireNonNull(yearMonth, "yearMonth no puede ser null");
    }

    public static PeriodoMensual of(int anio, int mes) {
        return new PeriodoMensual(YearMonth.of(anio, mes));
    }

    /**
     * Parsea el formato AAAAMM que devuelve la API-INDEC en el campo "periodo".
     */
    public static PeriodoMensual desdeCodigoIndec(String periodoAAAAMM) {
        Objects.requireNonNull(periodoAAAAMM, "periodo no puede ser null");
        if (periodoAAAAMM.length() != 6) {
            throw new IllegalArgumentException(
                    "Formato de período inválido, se esperaba AAAAMM: " + periodoAAAAMM);
        }
        int anio = Integer.parseInt(periodoAAAAMM.substring(0, 4));
        int mes = Integer.parseInt(periodoAAAAMM.substring(4, 6));
        return of(anio, mes);
    }

    public String codigoIndec() {
        return "%04d%02d".formatted(yearMonth.getYear(), yearMonth.getMonthValue());
    }

    public int anio() {
        return yearMonth.getYear();
    }

    public int mes() {
        return yearMonth.getMonthValue();
    }

    public PeriodoMensual mesAnterior() {
        return new PeriodoMensual(yearMonth.minusMonths(1));
    }

    public PeriodoMensual mismoMesAnioAnterior() {
        return new PeriodoMensual(yearMonth.minusYears(1));
    }

    /** Diciembre del año anterior a este período; base para la variación acumulada anual. */
    public PeriodoMensual diciembreAnioAnterior() {
        return of(yearMonth.getYear() - 1, 12);
    }

    public boolean esEnero() {
        return yearMonth.getMonthValue() == 1;
    }

    public boolean isBefore(PeriodoMensual other) {
        return this.yearMonth.isBefore(other.yearMonth);
    }

    public boolean isAfter(PeriodoMensual other) {
        return this.yearMonth.isAfter(other.yearMonth);
    }

    @Override
    public int compareTo(PeriodoMensual other) {
        return this.yearMonth.compareTo(other.yearMonth);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PeriodoMensual that)) return false;
        return yearMonth.equals(that.yearMonth);
    }

    @Override
    public int hashCode() {
        return yearMonth.hashCode();
    }

    @Override
    public String toString() {
        return codigoIndec();
    }
}
