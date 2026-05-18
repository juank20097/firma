/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.persistence.mongo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <b> Repositorio Spring Data MongoDB para registros de auditoría.
 * Solo se activa cuando MONGO_ENABLED=true en el .env.
 * Almacena documentos en la base de datos AUDITORIA_IESS. </b>
 *
 * @author Juan Carlos Estévez Hidalgo
 *
 * @version Revision: 1.0
 *          <p>
 *          [Author: Juan Carlos Estévez Hidalgo , Date: 07 may 2026]
 *          </p>
 */
@Repository
@ConditionalOnProperty(name = "MONGO_ENABLED", havingValue = "true")
public interface AuditoriaMongoRepository extends MongoRepository<AuditoriaDocument, String> {

    /**
     * <b> Busca registros de auditoría por usuario de aplicación. </b>
     *
     * @param audAppUser usuario de aplicación a buscar
     * @return lista de documentos de auditoría del usuario
     */
    List<AuditoriaDocument> findByAudAppUser(String audAppUser);

    /**
     * <b> Busca registros de auditoría por entidad y tipo de operación. </b>
     *
     * @param audEntity    nombre de la entidad auditada
     * @param audOperation tipo de operación: I, U o D
     * @return lista de documentos de auditoría
     */
    List<AuditoriaDocument> findByAudEntityAndAudOperation(String audEntity, String audOperation);

    /**
     * <b> Busca registros de auditoría en un rango de fechas. </b>
     *
     * @param desde fecha y hora de inicio del rango
     * @param hasta fecha y hora de fin del rango
     * @return lista de documentos de auditoría en el rango
     */
    List<AuditoriaDocument> findByAudTimestampBetween(LocalDateTime desde, LocalDateTime hasta);
}
