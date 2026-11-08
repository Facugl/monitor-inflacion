package ar.com.facundoluna.inflacion.infrastructure.persistence;

import ar.com.facundoluna.inflacion.domain.model.NivelGeneralIpc;
import ar.com.facundoluna.inflacion.domain.model.PeriodoMensual;
import ar.com.facundoluna.inflacion.domain.model.RegionIpc;
import ar.com.facundoluna.inflacion.domain.model.SerieTipo;
import ar.com.facundoluna.inflacion.infrastructure.persistence.adapter.IndicadorIpcRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de integración real: levanta un Postgres efímero con Testcontainers, corre
 * las migraciones Flyway del proyecto, y verifica que el adaptador persiste y
 * recupera los datos correctamente — sin mockear nada de la capa de persistencia.
 */
@Testcontainers
@SpringBootTest
@Transactional
class IndicadorIpcRepositoryAdapterIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("monitor_inflacion_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void propiedadesDinamicas(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("indec.api.token", () -> "token-de-prueba");
        registry.add("indec.sync.habilitado", () -> "false");
    }

    @Resource
    private IndicadorIpcRepositoryAdapter adapter;

    @Test
    void guardaYRecuperaLaSerieOrdenadaCronologicamente() {
        var enero = new NivelGeneralIpc(PeriodoMensual.of(2026, 1), RegionIpc.NACIONAL, SerieTipo.ORIGINAL, new BigDecimal("100.00"));
        var febrero = new NivelGeneralIpc(PeriodoMensual.of(2026, 2), RegionIpc.NACIONAL, SerieTipo.ORIGINAL, new BigDecimal("102.50"));

        adapter.guardarTodos(List.of(febrero, enero)); // a propósito, desordenado

        var serie = adapter.buscarSerie(RegionIpc.NACIONAL, SerieTipo.ORIGINAL, PeriodoMensual.of(2026, 1), PeriodoMensual.of(2026, 2));

        assertThat(serie).extracting(d -> d.periodo().mes()).containsExactly(1, 2);
        assertThat(adapter.ultimoPeriodoCargado()).contains(PeriodoMensual.of(2026, 2));
    }

    @Test
    void guardarTodosEsIdempotente() {
        var dato = new NivelGeneralIpc(PeriodoMensual.of(2026, 3), RegionIpc.NACIONAL, SerieTipo.ORIGINAL, new BigDecimal("105.00"));

        adapter.guardarTodos(List.of(dato));
        var datoActualizado = new NivelGeneralIpc(PeriodoMensual.of(2026, 3), RegionIpc.NACIONAL, SerieTipo.ORIGINAL, new BigDecimal("105.50"));
        adapter.guardarTodos(List.of(datoActualizado)); // re-sync del mismo período, con valor revisado

        var ultimo = adapter.buscarUltimo(RegionIpc.NACIONAL, SerieTipo.ORIGINAL);

        assertThat(ultimo).isPresent();
        assertThat(ultimo.get().indice()).isEqualByComparingTo("105.50");
    }
}
