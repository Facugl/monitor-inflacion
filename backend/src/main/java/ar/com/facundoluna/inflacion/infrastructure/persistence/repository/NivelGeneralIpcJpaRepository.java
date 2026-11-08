package ar.com.facundoluna.inflacion.infrastructure.persistence.repository;

import ar.com.facundoluna.inflacion.infrastructure.persistence.entity.NivelGeneralIpcEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NivelGeneralIpcJpaRepository extends JpaRepository<NivelGeneralIpcEntity, Long> {

    Optional<NivelGeneralIpcEntity> findByPeriodoAndRegionAndSerie(String periodo, String region, String serie);

    List<NivelGeneralIpcEntity> findByRegionAndSerieAndPeriodoBetweenOrderByPeriodoAsc(
            String region, String serie, String periodoDesde, String periodoHasta);

    Optional<NivelGeneralIpcEntity> findFirstByRegionAndSerieOrderByPeriodoDesc(String region, String serie);

    Optional<NivelGeneralIpcEntity> findFirstByOrderByPeriodoDesc();
}
