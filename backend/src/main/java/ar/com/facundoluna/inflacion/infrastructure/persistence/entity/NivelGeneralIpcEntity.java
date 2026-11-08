package ar.com.facundoluna.inflacion.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entidad JPA para la tabla nivel_general_ipc. No se expone fuera de la capa de
 * infraestructura: el resto del sistema trabaja con el modelo de dominio
 * {@link ar.com.facundoluna.inflacion.domain.model.NivelGeneralIpc}.
 */
@Entity
@Table(
        name = "nivel_general_ipc",
        uniqueConstraints = @UniqueConstraint(columnNames = {"periodo", "region", "serie"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NivelGeneralIpcEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Formato AAAAMM, igual que lo publica la API-INDEC. */
    @Column(nullable = false, length = 6)
    private String periodo;

    @Column(nullable = false, length = 20)
    private String region;

    @Column(nullable = false, length = 20)
    private String serie;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal indice;
}
