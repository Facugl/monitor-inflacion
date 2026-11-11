package ar.com.facundoluna.inflacion.application.port.out;

/**
 * La API-INDEC no está disponible en este momento: no contestó dentro del timeout
 * o devolvió un error de su lado.
 *
 * <p>Vive junto al puerto y no en {@code infrastructure.indec} a propósito: forma
 * parte del contrato del puerto, así que la capa web puede traducirla a un 503 sin
 * tener que conocer al adaptador ni a Spring Web Client.</p>
 *
 * <p>Es una condición transitoria y ajena al sistema. Se distingue de un error
 * propio porque la respuesta correcta es "volvé a intentar más tarde", no "hay un
 * bug acá".</p>
 */
public class IndecApiNoDisponibleException extends RuntimeException {

    public IndecApiNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
