package ar.com.facundoluna.inflacion.config;

import ar.com.facundoluna.inflacion.application.port.out.IndicadorIpcRepositoryPort;
import ar.com.facundoluna.inflacion.application.usecase.SincronizarIpcUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara la sincronización con la API-INDEC. Hay tres caminos hacia el mismo caso
 * de uso: el cron de {@code indec.sync.cron}, el arranque con la base vacía, y
 * {@code POST /api/v1/ipc/sync} a mano.
 */
@Component
public class SincronizacionProgramada {

    private static final Logger log = LoggerFactory.getLogger(SincronizacionProgramada.class);

    private final SincronizarIpcUseCase sincronizarIpcUseCase;
    private final IndicadorIpcRepositoryPort repositorio;
    private final boolean habilitado;
    private final boolean sincronizarAlIniciar;

    public SincronizacionProgramada(
            SincronizarIpcUseCase sincronizarIpcUseCase,
            IndicadorIpcRepositoryPort repositorio,
            org.springframework.core.env.Environment env) {
        this.sincronizarIpcUseCase = sincronizarIpcUseCase;
        this.repositorio = repositorio;
        this.habilitado = Boolean.parseBoolean(env.getProperty("indec.sync.habilitado", "true"));
        this.sincronizarAlIniciar = Boolean.parseBoolean(env.getProperty("indec.sync.al-iniciar", "true"));
    }

    /**
     * Carga inicial: si la base está vacía, trae la serie al arrancar.
     *
     * <p>Existe por el despliegue. Una instancia recién creada arranca con la base
     * vacía y, sin esto, el dashboard muestra "no hay datos" hasta el próximo cron.
     * La condición es "vacía", no "siempre": en un reinicio con datos no vuelve a
     * pegarle a la API-INDEC al pedo.</p>
     *
     * <p>Corre en {@link ApplicationReadyEvent}, o sea con el servidor ya
     * respondiendo, así que un health check no se queda esperando esto. Si falla,
     * se loguea y la app sigue viva: quedan el cron y el endpoint manual.</p>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void sincronizarSiEstaVacia() {
        if (!habilitado || !sincronizarAlIniciar) {
            return;
        }
        if (repositorio.ultimoPeriodoCargado().isPresent()) {
            log.info("La base ya tiene datos de IPC, se omite la sincronización inicial");
            return;
        }
        log.info("Base de IPC vacía, sincronizando con la API-INDEC al arrancar");
        try {
            var resultado = sincronizarIpcUseCase.ejecutar();
            log.info("Carga inicial de IPC completa: {} puntos de nivel general, {} de categorías",
                    resultado.puntosNivelGeneral(), resultado.puntosCategorias());
        } catch (Exception e) {
            log.error("Falló la carga inicial de IPC; quedan el cron y POST /api/v1/ipc/sync", e);
        }
    }

    @Scheduled(cron = "${indec.sync.cron}")
    public void sincronizar() {
        if (!habilitado) {
            return;
        }
        try {
            var resultado = sincronizarIpcUseCase.ejecutar();
            log.info("Sincronización programada de IPC completa: {} puntos de nivel general, {} de categorías",
                    resultado.puntosNivelGeneral(), resultado.puntosCategorias());
        } catch (Exception e) {
            log.error("Falló la sincronización programada de IPC", e);
        }
    }
}
