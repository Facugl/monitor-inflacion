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
        /** Nivel de la clasificación: "Nivel General" o "División". */
        String nivel,
        /** Código COICOP: "0" para el nivel general, "01".."12" para las divisiones. */
        String codigo,
        String descripcion,
        Double valor
) {
}
