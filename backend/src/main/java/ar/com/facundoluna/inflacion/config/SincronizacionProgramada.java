package ar.com.facundoluna.inflacion.config;

import ar.com.facundoluna.inflacion.application.usecase.SincronizarIpcUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara la sincronización con la API-INDEC según el cron configurado en
 * {@code indec.sync.cron}. Además del job programado, el mismo caso de uso se
 * puede disparar manualmente vía {@code POST /api/v1/ipc/sync}.
 */
@Component
public class SincronizacionProgramada {

    private static final Logger log = LoggerFactory.getLogger(SincronizacionProgramada.class);

    private final SincronizarIpcUseCase sincronizarIpcUseCase;
    private final boolean habilitado;

    public SincronizacionProgramada(
            SincronizarIpcUseCase sincronizarIpcUseCase,
            org.springframework.core.env.Environment env) {
        this.sincronizarIpcUseCase = sincronizarIpcUseCase;
        this.habilitado = Boolean.parseBoolean(env.getProperty("indec.sync.habilitado", "true"));
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
