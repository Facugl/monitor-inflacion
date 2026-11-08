package ar.com.facundoluna.inflacion.infrastructure.web.dto;

import ar.com.facundoluna.inflacion.domain.model.VariacionCategoriaIpc;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Variación de una categoría (división COICOP) del IPC en un período")
public record VariacionCategoriaResponse(
        @Schema(example = "202609", description = "Período en formato AAAAMM") String periodo,
        @Schema(example = "NACIONAL") String region,
        @Schema(example = "01", description = "Código de división COICOP") String codigo,
        @Schema(example = "Alimentos y bebidas no alcohólicas") String categoria,
        @Schema(description = "Número índice de la categoría") BigDecimal indice,
        @Schema(description = "Variación % respecto al mes anterior") BigDecimal variacionMensualPct,
        @Schema(description = "Variación % respecto al mismo mes del año anterior") BigDecimal variacionInteranualPct,
        @Schema(description = "Variación % acumulada en el año respecto a diciembre anterior") BigDecimal variacionAcumuladaAnioPct
) {
    public static VariacionCategoriaResponse desde(VariacionCategoriaIpc v) {
        return new VariacionCategoriaResponse(
                v.periodo().codigoIndec(),
                v.region().name(),
                v.categoria().codigo(),
                v.categoria().descripcion(),
                v.indice(),
                v.variacionMensualPct(),
                v.variacionInteranualPct(),
                v.variacionAcumuladaAnioPct()
        );
    }
}
