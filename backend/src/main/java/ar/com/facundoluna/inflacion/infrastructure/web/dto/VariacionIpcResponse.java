package ar.com.facundoluna.inflacion.infrastructure.web.dto;

import ar.com.facundoluna.inflacion.domain.model.VariacionIpc;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Punto de la serie de IPC con sus variaciones calculadas")
public record VariacionIpcResponse(
        @Schema(example = "202610", description = "Período en formato AAAAMM") String periodo,
        @Schema(example = "Nacional") String region,
        @Schema(example = "ORIGINAL") String serie,
        @Schema(description = "Número índice del nivel general") BigDecimal indice,
        @Schema(description = "Variación % respecto al mes anterior") BigDecimal variacionMensualPct,
        @Schema(description = "Variación % respecto al mismo mes del año anterior") BigDecimal variacionInteranualPct,
        @Schema(description = "Variación % acumulada en el año respecto a diciembre anterior") BigDecimal variacionAcumuladaAnioPct
) {
    public static VariacionIpcResponse desde(VariacionIpc v) {
        return new VariacionIpcResponse(
                v.periodo().codigoIndec(),
                v.region().name(),
                v.serie().name(),
                v.indice(),
                v.variacionMensualPct(),
                v.variacionInteranualPct(),
                v.variacionAcumuladaAnioPct()
        );
    }
}
