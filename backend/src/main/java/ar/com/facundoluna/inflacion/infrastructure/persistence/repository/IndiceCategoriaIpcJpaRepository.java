package ar.com.facundoluna.inflacion.infrastructure.persistence.repository;

import ar.com.facundoluna.inflacion.infrastructure.persistence.entity.IndiceCategoriaIpcEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IndiceCategoriaIpcJpaRepository extends JpaRepository<IndiceCategoriaIpcEntity, Long> {

    Optional<IndiceCategoriaIpcEntity> findByPeriodoAndRegionAndCategoriaCodigo(
            String periodo, String region, String categoriaCodigo);

    List<IndiceCategoriaIpcEntity> findByRegionAndPeriodoBetweenOrderByPeriodoAsc(
            String region, String periodoDesde, String periodoHasta);

    Optional<IndiceCategoriaIpcEntity> findFirstByOrderByPeriodoDesc();
}
