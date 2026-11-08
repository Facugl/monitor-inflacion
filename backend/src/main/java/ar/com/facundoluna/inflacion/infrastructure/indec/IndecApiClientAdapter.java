package ar.com.facundoluna.inflacion.infrastructure.indec;

import ar.com.facundoluna.inflacion.application.port.out.IndecApiClientPort;
import ar.com.facundoluna.inflacion.domain.model.NivelGeneralIpc;
import ar.com.facundoluna.inflacion.domain.model.PeriodoMensual;
import ar.com.facundoluna.inflacion.domain.model.RegionIpc;
import ar.com.facundoluna.inflacion.domain.model.SerieTipo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador de salida: implementa {@link IndecApiClientPort} llamando a la API-INDEC
 * real vía HTTP. Es la ÚNICA clase del proyecto que sabe que existe un JWT, un header
 * Authorization y el dialecto de filtros PostgREST — el resto del sistema (dominio y
 * aplicación) no depende de ningún detalle de la API-INDEC.
 *
 * <p>La API-INDEC pagina en bloques de hasta 1000 filas (ver documentación oficial),
 * así que recorremos páginas con {@code limit}/{@code offset} hasta agotar los datos.</p>
 */
@Component
public class IndecApiClientAdapter implements IndecApiClientPort {

    private static final Logger log = LoggerFactory.getLogger(IndecApiClientAdapter.class);
    private static final int TAMANIO_PAGINA = 1000;

    private final RestClient restClient;
    private final IndecApiProperties properties;

    public IndecApiClientAdapter(IndecApiProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.token())
                .build();
    }

    @Override
    public List<NivelGeneralIpc> obtenerNivelGeneralIpcNacional(PeriodoMensual desde) {
        List<NivelGeneralIpc> resultado = new ArrayList<>();
        int offset = 0;
        int recibidos;

        do {
            final int paginaOffset = offset;
            List<IpcNacionalRegistroDto> pagina = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/ipc_nacional")
                            .queryParam("periodo", "gte." + desde.codigoIndec())
                            .queryParam("region", "eq." + properties.regionNacional())
                            .queryParam("descripcion", "eq." + properties.nivelGeneralDescripcion())
                            .queryParam("tipo_de_indicador", "eq." + properties.tipoIndicadorIndice())
                            .queryParam("order", "periodo.asc")
                            .queryParam("limit", TAMANIO_PAGINA)
                            .queryParam("offset", paginaOffset)
                            .build())
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<IpcNacionalRegistroDto>>() {});

            if (pagina == null) {
                pagina = List.of();
            }

            recibidos = pagina.size();
            for (IpcNacionalRegistroDto dto : pagina) {
                mapearADominio(dto).ifPresent(resultado::add);
            }
            offset += TAMANIO_PAGINA;
        } while (recibidos == TAMANIO_PAGINA);

        log.info("Sincronizados {} puntos de nivel general IPC nacional desde {}", resultado.size(), desde);
        return resultado;
    }

    private java.util.Optional<NivelGeneralIpc> mapearADominio(IpcNacionalRegistroDto dto) {
        if (dto.valor() == null) {
            log.warn("Registro de IPC sin valor para período {}, se omite", dto.periodo());
            return java.util.Optional.empty();
        }
        try {
            return java.util.Optional.of(new NivelGeneralIpc(
                    PeriodoMensual.desdeCodigoIndec(dto.periodo()),
                    RegionIpc.NACIONAL,
                    SerieTipo.ORIGINAL,
                    BigDecimal.valueOf(dto.valor())
            ));
        } catch (RuntimeException e) {
            log.warn("No se pudo mapear el registro de IPC del período {}: {}", dto.periodo(), e.getMessage());
            return java.util.Optional.empty();
        }
    }
}
