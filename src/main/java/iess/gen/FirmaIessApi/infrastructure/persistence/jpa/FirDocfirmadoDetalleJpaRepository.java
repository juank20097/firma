/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * <b>Repositorio JPA para la entidad FirDocfirmadoDetalleEntity.</b>
 *
 * <p>
 * Esta interfaz forma parte de la capa de infraestructura y proporciona
 * acceso a la base de datos para la gestiÃ³n de los detalles asociados
 * a documentos firmados digitalmente.
 * </p>
 *
 *
 * @author Juan Carlos EstÃ©vez Hidalgo
 *
 * @version Revision: 1.0
 *          <p>
 *          [Author: Juan Carlos EstÃ©vez Hidalgo , Date: 13 may 2026]
 *          </p>
 */
@Repository
public interface FirDocfirmadoDetalleJpaRepository extends JpaRepository<FirDocfirmadoDetalleEntity, UUID> {

}
