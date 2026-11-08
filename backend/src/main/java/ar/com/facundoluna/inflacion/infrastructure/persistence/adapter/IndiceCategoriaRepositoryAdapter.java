package ar.com.facundoluna.inflacion.infrastructure.persistence.adapter;

import ar.com.facundoluna.inflacion.application.port.out.IndiceCategoriaRepositoryPort;
import ar.com.facundoluna.inflacion.domain.model.CategoriaIpc;
import ar.com.facundoluna.inflacion.domain.model.IndiceCategoriaIpc;
import ar.com.facundoluna.inflacion.domain.model.PeriodoMensual;
import ar.com.facundoluna.inflacion.domain.model.RegionIpc;
import ar.com.facundoluna.inflacion.infrastructure.persistence.entity.IndiceCategoriaIpcEntity;
import ar.com.facundoluna.inflacion.infrastructure.persistence.repository.IndiceCategoriaIpcJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida: implementa {@link IndiceCategoriaRepositoryPort} usando Spring
 * Data JPA. Único lugar que traduce entre el dominio ({@link IndiceCategoriaIpc}) y la
 * entidad de persistencia.
 */
@Component
public class IndiceCategoriaRepositoryAdapter implements IndiceCategoriaRepositoryPort {

    private final IndiceCategoriaIpcJpaRepository jpaRepository;

    public IndiceCategoriaRepositoryAdapter(IndiceCategoriaIpcJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public void guardarTodos(List<IndiceCategoriaIpc> datos) {
        for (IndiceCategoriaIpc dato : datos) {
            String periodo = dato.periodo().codigoIndec();
            String region = dato.region().name();
            String codigo = dato.categoria().codigo();

            IndiceCategoriaIpcEntity entity = jpaRepository
                    .findByPeriodoAndRegionAndCategoriaCodigo(periodo, region, codigo)
                    .orElseGet(IndiceCategoriaIpcEntity::new);

            entity.setPeriodo(periodo);
            entity.setRegion(region);
            entity.setCategoriaCodigo(codigo);
            // Se re-escribe la descripción en cada sync: si el INDEC le cambia la
            // redacción a una división, la base queda con el texto vigente.
            entity.setCategoriaDescripcion(dato.categoria().descripcion());
            entity.setIndice(dato.indice());

            jpaRepository.save(entity);
        }
    }

    @Override
    public List<IndiceCategoriaIpc> buscarSerie(RegionIpc region, PeriodoMensual desde, PeriodoMensual hasta) {
        return jpaRepository
                .findByRegionAndPeriodoBetweenOrderByPeriodoAsc(
                        region.name(), desde.codigoIndec(), hasta.codigoIndec())
                .stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public Optional<PeriodoMensual> ultimoPeriodoCargado() {
        return jpaRepository.findFirstByOrderByPeriodoDesc()
                .map(e -> PeriodoMensual.desdeCodigoIndec(e.getPeriodo()));
    }

    private IndiceCategoriaIpc aDominio(IndiceCategoriaIpcEntity entity) {
        return new IndiceCategoriaIpc(
                PeriodoMensual.desdeCodigoIndec(entity.getPeriodo()),
                RegionIpc.valueOf(entity.getRegion()),
                new CategoriaIpc(entity.getCategoriaCodigo(), entity.getCategoriaDescripcion()),
                entity.getIndice()
        );
    }
}
