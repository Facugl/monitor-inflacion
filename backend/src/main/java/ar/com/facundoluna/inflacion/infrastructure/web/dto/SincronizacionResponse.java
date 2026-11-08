package ar.com.facundoluna.inflacion.infrastructure.web.dto;

import ar.com.facundoluna.inflacion.application.usecase.ResultadoSincronizacion;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado de una sincronización con la API-INDEC")
public record SincronizacionResponse(
        @Schema(description = "Total de puntos procesados entre las dos series") int puntosSincronizados,
        @Schema(description = "Puntos del nivel general") int puntosNivelGeneral,
        @Schema(description = "Puntos de la apertura por categoría") int puntosCategorias,
        String mensaje
) {
    public static SincronizacionResponse desde(ResultadoSincronizacion resultado) {
        String mensaje = "Sincronización completa: %d puntos de nivel general y %d de categorías"
                .formatted(resultado.puntosNivelGeneral(), resultado.puntosCategorias());

        return new SincronizacionResponse(
                resultado.total(),
                resultado.puntosNivelGeneral(),
                resultado.puntosCategorias(),
                mensaje
        );
    }
}
