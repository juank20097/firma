/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad JPA para la tabla public.sistema de firmadigital.
 */
@Entity
@Table(name = "sistema", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SistemaEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "url")
    private String url;

    @Column(name = "apikey")
    private String apikey;

    @Column(name = "apikeyrest")
    private String apikeyrest;
}
