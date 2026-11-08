package ar.com.facundoluna.inflacion.infrastructure.indec;

/**
 * DTO que refleja, campo por campo, un registro del endpoint {@code /ipc_nacional}
 * de la API-INDEC (ver esquema "ipc_nacional" en el OpenAPI del catálogo). Solo mapea
 * los campos que este proyecto usa; la API trae más columnas.
 */
public record IpcNacionalRegistroDto(
        String periodo,
        Integer anio,
        Integer mes,
        String indicador,
        String tipo_de_indicador,
        String region,
        String descripcion,
        Double valor
) {
}
