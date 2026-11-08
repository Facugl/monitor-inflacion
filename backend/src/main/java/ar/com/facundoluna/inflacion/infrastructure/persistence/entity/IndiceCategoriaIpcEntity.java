package ar.com.facundoluna.inflacion.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entidad JPA para la tabla indice_categoria_ipc. No se expone fuera de la capa de
 * infraestructura: el resto del sistema trabaja con el modelo de dominio
 * {@link ar.com.facundoluna.inflacion.domain.model.IndiceCategoriaIpc}.
 */
@Entity
@Table(
        name = "indice_categoria_ipc",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"periodo", "region", "categoria_codigo"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IndiceCategoriaIpcEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Formato AAAAMM, igual que lo publica la API-INDEC. */
    @Column(nullable = false, length = 6)
    private String periodo;

    @Column(nullable = false, length = 20)
    private String region;

    /** Código de división COICOP: "01".."12". */
    @Column(name = "categoria_codigo", nullable = false, length = 10)
    private String categoriaCodigo;

    @Column(name = "categoria_descripcion", nullable = false, length = 200)
    private String categoriaDescripcion;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal indice;
}
