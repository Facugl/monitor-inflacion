package ar.com.facundoluna.inflacion.domain.model;

import java.util.Objects;

/**
 * Value object: una de las doce divisiones de la clasificación COICOP Argentina 2019
 * sobre las que el INDEC abre el IPC (alimentos, vivienda, salud, transporte, etc.).
 *
 * <p>Se modela como par código + descripción en vez de como {@code enum} a propósito:
 * el código ("01".."12") es estable, pero las descripciones las publica un tercero y
 * pueden cambiar de redacción. Con un enum, un cambio de texto del INDEC obligaría a
 * recompilar; así solo cambia el dato.</p>
 */
public record CategoriaIpc(String codigo, String descripcion) implements Comparable<CategoriaIpc> {

    public CategoriaIpc {
        Objects.requireNonNull(codigo, "codigo no puede ser null");
        Objects.requireNonNull(descripcion, "descripcion no puede ser null");
        if (codigo.isBlank()) {
            throw new IllegalArgumentException("El código de categoría no puede estar vacío");
        }
        if (descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción de la categoría no puede estar vacía");
        }
    }

    /**
     * El orden natural es por código, que es el de la clasificación COICOP. Sirve para
     * desempatar rankings de forma determinística cuando dos categorías varían igual.
     */
    @Override
    public int compareTo(CategoriaIpc otra) {
        return this.codigo.compareTo(otra.codigo);
    }

    @Override
    public String toString() {
        return "%s - %s".formatted(codigo, descripcion);
    }
}
