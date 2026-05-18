/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad SistemaEntity.
 */
@Repository
public interface SistemaJpaRepository extends JpaRepository<SistemaEntity, Long> {

    /**
     * Busca un sistema por su nombre.
     *
     * @param nombre nombre del sistema
     * @return sistema encontrado
     */
    Optional<SistemaEntity> findByNombre(String nombre);
}
