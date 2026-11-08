package ar.com.facundoluna.inflacion.infrastructure.indec;

import ar.com.facundoluna.inflacion.application.port.out.IndecApiClientPort;
import ar.com.facundoluna.inflacion.domain.model.CategoriaIpc;
import ar.com.facundoluna.inflacion.domain.model.IndiceCategoriaIpc;
import ar.com.facundoluna.inflacion.domain.model.NivelGeneralIpc;
import ar.com.facundoluna.inflacion.domain.model.PeriodoMensual;
import ar.com.facundoluna.inflacion.domain.model.RegionIpc;
import ar.com.facundoluna.inflacion.domain.model.SerieTipo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

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
    private static final ParameterizedTypeReference<List<IpcNacionalRegistroDto>> TIPO_PAGINA =
            new ParameterizedTypeReference<>() {};

    /*
     * El orden DEBE ser total (sin empates) para que la paginación por limit/offset sea
     * correcta: si dos filas empatan, el servidor puede devolverlas en distinto orden en
     * cada página y, en el borde, una fila se repite y otra se pierde.
     *
     * Para el nivel general alcanza con el período (hay una sola fila por período), pero
     * las categorías traen 12 filas con el MISMO período, así que hay que desempatar por
     * código. Sin esto se pierden silenciosamente las filas del período que cae en el
     * límite de página.
     */
    private static final String ORDEN_NIVEL_GENERAL = "periodo.asc";
    private static final String ORDEN_CATEGORIAS = "periodo.asc,codigo.asc";

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
        List<NivelGeneralIpc> resultado = traerTodo(
                desde,
                Map.of("descripcion", "eq." + properties.nivelGeneralDescripcion()),
                ORDEN_NIVEL_GENERAL,
                this::aNivelGeneral);

        log.info("Sincronizados {} puntos de nivel general IPC nacional desde {}", resultado.size(), desde);
        return resultado;
    }

    @Override
    public List<IndiceCategoriaIpc> obtenerIndicesPorCategoriaNacional(PeriodoMensual desde) {
        List<IndiceCategoriaIpc> resultado = traerTodo(
                desde,
                Map.of("nivel", "eq." + properties.nivelDivision()),
                ORDEN_CATEGORIAS,
                this::aIndiceCategoria);

        log.info("Sincronizados {} puntos de IPC por categoría nacional desde {}", resultado.size(), desde);
        return resultado;
    }

    /**
     * Recorre todas las páginas de {@code /ipc_nacional} aplicando los filtros comunes
     * (período, región nacional, tipo "índice") más los {@code filtrosExtra} propios de
     * cada serie, y mapea cada registro al dominio con {@code mapeador}.
     *
     * <p>Los registros que el mapeador no puede convertir se descartan con un warning:
     * un dato suelto mal formado del INDEC no debe tirar abajo toda la sincronización.</p>
     */
    private <T> List<T> traerTodo(
            PeriodoMensual desde,
            Map<String, String> filtrosExtra,
            String orden,
            Function<IpcNacionalRegistroDto, Optional<T>> mapeador) {

        List<T> resultado = new ArrayList<>();
        int offset = 0;
        int recibidos;

        do {
            final int paginaOffset = offset;
            List<IpcNacionalRegistroDto> pagina = restClient.get()
                    .uri(uriBuilder -> armarUri(uriBuilder, desde, filtrosExtra, orden, paginaOffset))
                    .retrieve()
                    .body(TIPO_PAGINA);

            if (pagina == null) {
                pagina = List.of();
            }

            recibidos = pagina.size();
            for (IpcNacionalRegistroDto dto : pagina) {
                mapeador.apply(dto).ifPresent(resultado::add);
            }
            offset += TAMANIO_PAGINA;
        } while (recibidos == TAMANIO_PAGINA);

        return resultado;
    }

    private java.net.URI armarUri(
            UriBuilder uriBuilder, PeriodoMensual desde, Map<String, String> filtrosExtra,
            String orden, int offset) {

        uriBuilder.path("/ipc_nacional")
                .queryParam("periodo", "gte." + desde.codigoIndec())
                .queryParam("region", "eq." + properties.regionNacional())
                .queryParam("tipo_de_indicador", "eq." + properties.tipoIndicadorIndice())
                .queryParam("order", orden)
                .queryParam("limit", TAMANIO_PAGINA)
                .queryParam("offset", offset);

        filtrosExtra.forEach(uriBuilder::queryParam);

        return uriBuilder.build();
    }

    private Optional<NivelGeneralIpc> aNivelGeneral(IpcNacionalRegistroDto dto) {
        if (dto.valor() == null) {
            log.warn("Registro de IPC sin valor para período {}, se omite", dto.periodo());
            return Optional.empty();
        }
        try {
            return Optional.of(new NivelGeneralIpc(
                    PeriodoMensual.desdeCodigoIndec(dto.periodo()),
                    RegionIpc.NACIONAL,
                    SerieTipo.ORIGINAL,
                    BigDecimal.valueOf(dto.valor())
            ));
        } catch (RuntimeException e) {
            log.warn("No se pudo mapear el registro de IPC del período {}: {}", dto.periodo(), e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<IndiceCategoriaIpc> aIndiceCategoria(IpcNacionalRegistroDto dto) {
        if (dto.valor() == null) {
            log.warn("Registro de categoría sin valor para período {}, se omite", dto.periodo());
            return Optional.empty();
        }
        try {
            return Optional.of(new IndiceCategoriaIpc(
                    PeriodoMensual.desdeCodigoIndec(dto.periodo()),
                    RegionIpc.NACIONAL,
                    new CategoriaIpc(dto.codigo(), dto.descripcion()),
                    BigDecimal.valueOf(dto.valor())
            ));
        } catch (RuntimeException e) {
            log.warn("No se pudo mapear la categoría '{}' del período {}: {}",
                    dto.descripcion(), dto.periodo(), e.getMessage());
            return Optional.empty();
        }
    }
}
