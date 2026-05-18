/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad VersionEntity.
 */
@Repository
public interface VersionJpaRepository extends JpaRepository<VersionEntity, Long> {

    /**
     * Busca la primera versiÃ³n habilitada por aplicaciÃ³n y sistema operativo.
     *
     * @param aplicacion      nombre de la aplicaciÃ³n
     * @param sistemaOperativo sistema operativo
     * @param status          estado habilitado
     * @return versiÃ³n encontrada
     */
    Optional<VersionEntity> findFirstByAplicacionAndSistemaOperativoAndStatus(
            String aplicacion, String sistemaOperativo, Boolean status);
}
