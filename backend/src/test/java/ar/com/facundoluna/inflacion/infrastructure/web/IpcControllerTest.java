package ar.com.facundoluna.inflacion.infrastructure.web;

import ar.com.facundoluna.inflacion.application.usecase.ObtenerEvolucionIpcUseCase;
import ar.com.facundoluna.inflacion.application.usecase.ObtenerResumenIpcUseCase;
import ar.com.facundoluna.inflacion.application.usecase.SincronizarIpcUseCase;
import ar.com.facundoluna.inflacion.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IpcController.class)
class IpcControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SincronizarIpcUseCase sincronizarIpcUseCase;

    @MockBean
    private ObtenerEvolucionIpcUseCase obtenerEvolucionIpcUseCase;

    @MockBean
    private ObtenerResumenIpcUseCase obtenerResumenIpcUseCase;

    @Test
    void sincronizarDevuelveLaCantidadDePuntosProcesados() throws Exception {
        given(sincronizarIpcUseCase.ejecutar()).willReturn(14);

        mockMvc.perform(post("/api/v1/ipc/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puntosSincronizados").value(14));
    }

    @Test
    void evolucionDevuelveLaSerieDeVariaciones() throws Exception {
        var variacion = new VariacionIpc(
                PeriodoMensual.of(2026, 10), RegionIpc.NACIONAL, SerieTipo.ORIGINAL,
                new BigDecimal("250.00"), new BigDecimal("2.50"), new BigDecimal("31.40"), new BigDecimal("27.90"));

        given(obtenerEvolucionIpcUseCase.ejecutar(any(), any(), any(), any())).willReturn(List.of(variacion));

        mockMvc.perform(get("/api/v1/ipc/evolucion")
                        .param("desde", "202601")
                        .param("hasta", "202610"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].periodo").value("202610"))
                .andExpect(jsonPath("$[0].variacionMensualPct").value(2.50));
    }

    @Test
    void resumenDevuelve404SiTodaviaNoHayDatos() throws Exception {
        given(obtenerResumenIpcUseCase.ejecutar(any(), any()))
                .willThrow(new NoSuchElementException("Todavía no hay datos de IPC sincronizados"));

        mockMvc.perform(get("/api/v1/ipc/resumen"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Todavía no hay datos de IPC sincronizados"));
    }
}
