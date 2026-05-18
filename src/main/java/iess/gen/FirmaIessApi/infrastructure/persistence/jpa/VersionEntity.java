/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad JPA para la tabla public.version de firmadigital.
 */
@Entity
@Table(name = "version", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VersionEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "version")
    private String version;

    @Column(name = "aplicacion")
    private String aplicacion;

    @Column(name = "sistemaoperativo")
    private String sistemaOperativo;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "fechaliberacion")
    private LocalDateTime fechaLiberacion;

    @Column(name = "fechaobsoleto")
    private LocalDateTime fechaObsoleto;

    @Column(name = "descripcion")
    private String descripcion;
}
