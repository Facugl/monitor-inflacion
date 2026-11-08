package ar.com.facundoluna.inflacion.infrastructure.indec;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración del cliente de la API-INDEC. Los valores de {@code nivelGeneralDescripcion}
 * y {@code tipoIndicadorIndice} son los literales que usa el endpoint {@code /ipc_nacional}
 * para identificar la fila de "nivel general" y el tipo de dato "índice" (en vez de
 * variación intermensual/interanual, que INDEC también publica en la misma tabla).
 *
 * <p>Son configurables porque son cadenas de texto publicadas por un tercero: si INDEC
 * las cambia, se ajustan acá sin tocar código. Verificalas la primera vez contra una
 * respuesta real (ver README, sección "Verificar la estructura de datos").</p>
 */
@ConfigurationProperties(prefix = "indec.api")
public record IndecApiProperties(
        String baseUrl,
        String token,
        String nivelGeneralDescripcion,
        String tipoIndicadorIndice,
        String regionNacional
) {
    public IndecApiProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.indec.gob.ar/v1";
        }
        if (nivelGeneralDescripcion == null || nivelGeneralDescripcion.isBlank()) {
            nivelGeneralDescripcion = "Nivel general";
        }
        if (tipoIndicadorIndice == null || tipoIndicadorIndice.isBlank()) {
            tipoIndicadorIndice = "Índice";
        }
        if (regionNacional == null || regionNacional.isBlank()) {
            regionNacional = "Nacional";
        }
    }
}
