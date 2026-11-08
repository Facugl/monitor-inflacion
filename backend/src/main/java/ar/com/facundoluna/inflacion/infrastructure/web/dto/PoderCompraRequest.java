package ar.com.facundoluna.inflacion.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Datos para simular la evolución del poder de compra de un sueldo nominal fijo")
public record PoderCompraRequest(

        @Schema(example = "202401", description = "Período en que empezaste a cobrar ese sueldo (AAAAMM)")
        @NotNull
        @Pattern(regexp = "\\d{6}", message = "El período debe tener formato AAAAMM")
        String periodoInicial,

        @Schema(example = "800000", description = "Sueldo nominal, constante desde el período inicial")
        @NotNull
        @Positive
        BigDecimal sueldoNominal
) {
}
