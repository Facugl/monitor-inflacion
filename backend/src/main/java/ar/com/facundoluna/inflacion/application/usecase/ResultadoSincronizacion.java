package ar.com.facundoluna.inflacion.application.usecase;

/**
 * Resultado de una corrida de {@link SincronizarIpcUseCase}: cuántos puntos se
 * trajeron de cada serie. Se devuelven desglosados y no como un único total porque
 * las dos series avanzan por separado — el INDEC puede publicar el nivel general y
 * las aperturas por categoría en momentos distintos.
 */
public record ResultadoSincronizacion(int puntosNivelGeneral, int puntosCategorias) {

    public int total() {
        return puntosNivelGeneral + puntosCategorias;
    }
}
