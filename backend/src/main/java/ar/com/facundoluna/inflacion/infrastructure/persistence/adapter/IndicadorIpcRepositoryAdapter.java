package ar.com.facundoluna.inflacion.infrastructure.persistence.adapter;

import ar.com.facundoluna.inflacion.application.port.out.IndicadorIpcRepositoryPort;
import ar.com.facundoluna.inflacion.domain.model.NivelGeneralIpc;
import ar.com.facundoluna.inflacion.domain.model.PeriodoMensual;
import ar.com.facundoluna.inflacion.domain.model.RegionIpc;
import ar.com.facundoluna.inflacion.domain.model.SerieTipo;
import ar.com.facundoluna.inflacion.infrastructure.persistence.entity.NivelGeneralIpcEntity;
import ar.com.facundoluna.inflacion.infrastructure.persistence.repository.NivelGeneralIpcJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida: implementa el puerto {@link IndicadorIpcRepositoryPort} usando
 * Spring Data JPA. Es el único lugar del sistema que traduce entre el modelo de
 * dominio (NivelGeneralIpc) y la entidad de persistencia (NivelGeneralIpcEntity).
 */
@Component
public class IndicadorIpcRepositoryAdapter implements IndicadorIpcRepositoryPort {

    private final NivelGeneralIpcJpaRepository jpaRepository;

    public IndicadorIpcRepositoryAdapter(NivelGeneralIpcJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public void guardarTodos(List<NivelGeneralIpc> datos) {
        for (NivelGeneralIpc dato : datos) {
            String periodo = dato.periodo().codigoIndec();
            String region = dato.region().name();
            String serie = dato.serie().name();

            NivelGeneralIpcEntity entity = jpaRepository
                    .findByPeriodoAndRegionAndSerie(periodo, region, serie)
                    .orElseGet(NivelGeneralIpcEntity::new);

            entity.setPeriodo(periodo);
            entity.setRegion(region);
            entity.setSerie(serie);
            entity.setIndice(dato.indice());

            jpaRepository.save(entity);
        }
    }

    @Override
    public List<NivelGeneralIpc> buscarSerie(RegionIpc region, SerieTipo serie, PeriodoMensual desde, PeriodoMensual hasta) {
        return jpaRepository
                .findByRegionAndSerieAndPeriodoBetweenOrderByPeriodoAsc(
                        region.name(), serie.name(), desde.codigoIndec(), hasta.codigoIndec())
                .stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public Optional<NivelGeneralIpc> buscarUltimo(RegionIpc region, SerieTipo serie) {
        return jpaRepository
                .findFirstByRegionAndSerieOrderByPeriodoDesc(region.name(), serie.name())
                .map(this::aDominio);
    }

    @Override
    public Optional<PeriodoMensual> ultimoPeriodoCargado() {
        return jpaRepository.findFirstByOrderByPeriodoDesc()
                .map(e -> PeriodoMensual.desdeCodigoIndec(e.getPeriodo()));
    }

    private NivelGeneralIpc aDominio(NivelGeneralIpcEntity entity) {
        return new NivelGeneralIpc(
                PeriodoMensual.desdeCodigoIndec(entity.getPeriodo()),
                RegionIpc.valueOf(entity.getRegion()),
                SerieTipo.valueOf(entity.getSerie()),
                entity.getIndice()
        );
    }
}
