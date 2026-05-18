/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * <b>Repositorio JPA para la entidad FirDocfirmadoEntity.</b>
 *
 * <p>
 * Provee operaciones de acceso a la base de datos para la gestiÃ³n
 * de documentos firmados digitalmente.
 * </p>
 *
 * <p>
 * Esta interfaz utiliza Spring Data JPA y permite consultas derivadas
 * por nombre de mÃ©todo sin necesidad de @Query en casos simples.
 * </p>
 *
 * @author Juan Carlos EstÃ©vez Hidalgo
 *
 * @version Revision: 1.0
 *          <p>
 *          [Author: Juan Carlos EstÃ©vez Hidalgo , Date: 13 may 2026]
 *          </p>
 */
@Repository
public interface FirDocfirmadoJpaRepository extends JpaRepository<FirDocfirmadoEntity, UUID> {

    /**
     * <b>Busca documentos firmados por cÃ©dula y lista de nombres de documentos.</b>
     *
     * <p>
     * Retorna todos los documentos firmados que coincidan con la cÃ©dula
     * del firmante y cuyo nombre de documento estÃ© dentro de la lista enviada.
     * </p>
     *
     * @param cedula  nÃºmero de cÃ©dula del firmante
     * @param nombres lista de nombres de documentos a filtrar
     * @return lista de documentos firmados encontrados
     */
    List<FirDocfirmadoEntity> findByCedulaAndNombreDocumentoIn(String cedula, List<String> nombres);

    /**
     * <b>Busca documentos firmados por cÃ©dula, lista de nombres y fecha de creaciÃ³n posterior a una fecha dada.</b>
     *
     * @param cedula      nÃºmero de cÃ©dula del firmante
     * @param nombres     lista de nombres de documentos a filtrar
     * @param fechaInicio fecha mÃ­nima de creaciÃ³n del registro
     * @return lista de documentos firmados encontrados
     */
    List<FirDocfirmadoEntity> findByCedulaAndNombreDocumentoInAndCreatedAtAfter(
            String cedula, List<String> nombres, java.time.LocalDateTime fechaInicio);

    /**
     * <b>Busca documentos firmados por lista de IDs y cÃ©dula del firmante.</b>
     *
     * <p>
     * Permite obtener mÃºltiples registros filtrando por identificadores
     * de documentos firmados y la cÃ©dula del titular.
     * </p>
     *
     * @param ids    lista de identificadores UUID de documentos
     * @param cedula nÃºmero de cÃ©dula del firmante
     * @return lista de documentos firmados encontrados
     */
    List<FirDocfirmadoEntity> findByIdDocInAndCedula(List<UUID> ids, String cedula);
}
