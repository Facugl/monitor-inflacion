package ar.com.facundoluna.inflacion.infrastructure.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApiInfo() {
        return new OpenAPI().info(new Info()
                .title("Monitor de Inflación Argentina")
                .version("1.0.0")
                .description("""
                        API que sincroniza el nivel general del IPC desde la API pública del INDEC \
                        y expone variaciones mensual, interanual y acumulada, además de un simulador \
                        de poder de compra de un sueldo nominal fijo en el tiempo.""")
                .contact(new Contact().name("Facundo Luna").url("https://github.com/Facugl"))
                .license(new License().name("MIT")));
    }
}
