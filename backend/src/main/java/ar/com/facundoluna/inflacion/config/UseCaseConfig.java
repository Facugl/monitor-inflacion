package ar.com.facundoluna.inflacion.config;

import ar.com.facundoluna.inflacion.application.port.out.IndecApiClientPort;
import ar.com.facundoluna.inflacion.application.port.out.IndiceCategoriaRepositoryPort;
import ar.com.facundoluna.inflacion.application.port.out.IndicadorIpcRepositoryPort;
import ar.com.facundoluna.inflacion.application.usecase.CalcularPoderCompraUseCase;
import ar.com.facundoluna.inflacion.application.usecase.ObtenerEvolucionIpcUseCase;
import ar.com.facundoluna.inflacion.application.usecase.ObtenerRankingCategoriasUseCase;
import ar.com.facundoluna.inflacion.application.usecase.ObtenerResumenIpcUseCase;
import ar.com.facundoluna.inflacion.application.usecase.SincronizarIpcUseCase;
import ar.com.facundoluna.inflacion.domain.service.CalculadoraPoderCompra;
import ar.com.facundoluna.inflacion.domain.service.CalculadoraVariacionesIpc;
import ar.com.facundoluna.inflacion.domain.service.CalculadoraVariacionesPorCategoria;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cablea los casos de uso (capa de aplicación) con los adaptadores de salida
 * (capa de infraestructura), y expone los servicios de dominio puros como beans.
 * Es la única clase que "conoce" tanto a la aplicación como a la infraestructura:
 * el resto del hexágono se comunica exclusivamente a través de puertos.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public CalculadoraVariacionesIpc calculadoraVariacionesIpc() {
        return new CalculadoraVariacionesIpc();
    }

    @Bean
    public CalculadoraPoderCompra calculadoraPoderCompra() {
        return new CalculadoraPoderCompra();
    }

    @Bean
    public CalculadoraVariacionesPorCategoria calculadoraVariacionesPorCategoria() {
        return new CalculadoraVariacionesPorCategoria();
    }

    @Bean
    public SincronizarIpcUseCase sincronizarIpcUseCase(
            IndecApiClientPort indecApiClient,
            IndicadorIpcRepositoryPort repositorioNivelGeneral,
            IndiceCategoriaRepositoryPort repositorioCategorias) {
        return new SincronizarIpcUseCase(indecApiClient, repositorioNivelGeneral, repositorioCategorias);
    }

    @Bean
    public ObtenerRankingCategoriasUseCase obtenerRankingCategoriasUseCase(
            IndiceCategoriaRepositoryPort repositorio, CalculadoraVariacionesPorCategoria calculadora) {
        return new ObtenerRankingCategoriasUseCase(repositorio, calculadora);
    }

    @Bean
    public ObtenerEvolucionIpcUseCase obtenerEvolucionIpcUseCase(
            IndicadorIpcRepositoryPort repositorio, CalculadoraVariacionesIpc calculadora) {
        return new ObtenerEvolucionIpcUseCase(repositorio, calculadora);
    }

    @Bean
    public ObtenerResumenIpcUseCase obtenerResumenIpcUseCase(
            IndicadorIpcRepositoryPort repositorio, CalculadoraVariacionesIpc calculadora) {
        return new ObtenerResumenIpcUseCase(repositorio, calculadora);
    }

    @Bean
    public CalcularPoderCompraUseCase calcularPoderCompraUseCase(
            IndicadorIpcRepositoryPort repositorio, CalculadoraPoderCompra calculadora) {
        return new CalcularPoderCompraUseCase(repositorio, calculadora);
    }
}
