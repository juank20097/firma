/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.application.usecase;

import iess.gen.FirmaIessApi.infrastructure.persistence.jpa.FirDocfirmadoJpaRepository;
import iess.gen.FirmaIessApi.infrastructure.mapper.FirDocfirmadoMapper;
import iess.gen.FirmaIessApi.model.FirDocfirmado;
import iess.gen.FirmaIessApi.application.port.StoragePort;
import iess.gen.FirmaIessApi.infrastructure.config.LoteContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b> Caso de uso que orquesta las operaciones de negocio sobre FirDocfirmado.
 * Gestiona la creaciÃ³n del registro en BD y el almacenamiento temporal del PDF firmado en MinIO. </b>
 *
 * @author Juan Carlos EstÃ©vez Hidalgo
 *
 * @version Revision: 2.0
 *          <p>
 *          [Author: Juan Carlos EstÃ©vez Hidalgo , Date: 14 may 2026]
 *          </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FirDocfirmadoUseCase {

    /** Repositorio JPA para operaciones de persistencia. */
    private final FirDocfirmadoJpaRepository repository;

    /** Mapper para conversiÃ³n entre entidad JPA, dominio y DTOs. */
    private final FirDocfirmadoMapper mapper;

    /** Puerto de almacenamiento (MinIO). */
    private final StoragePort storagePort;

    /** Holder del contexto de lote para obtener el idLote en el callback. */
    private final LoteContextHolder loteContextHolder;

    /**
     * Registra un documento firmado en BD y sube el PDF a MinIO.
     * Obtiene el idLote del LoteContextHolder usando la cÃ©dula.
     */
    @Transactional
    public FirDocfirmado crear(FirDocfirmado documento) {
        String idLote = loteContextHolder.obtener(documento.getCedula());
        return crear(documento, idLote);
    }

    /**
     * <b>
     * Registra un documento firmado en BD y sube el PDF a MinIO bajo la carpeta del lote.
     * </b>
     *
     * @param documento modelo de dominio del documento firmado
     * @param idLote    identificador del lote para agrupar en MinIO
     * @return documento firmado almacenado
     */
    @Transactional
    public FirDocfirmado crear(FirDocfirmado documento, String idLote) {
        documento.setStatus("A");

        // 1. Guardar en BD (sin el archivo)
        FirDocfirmado guardado = mapper.toDomain(repository.save(mapper.toEntity(documento)));

        // 2. Subir PDF a MinIO si viene el archivo
        if (documento.getArchivo() != null && documento.getArchivo().length > 0) {
            try {
                String fecha = java.time.LocalDate.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String carpeta = idLote != null
                        ? "temp_" + idLote
                        : "temp_" + guardado.getIdDoc();
                String nombreSinExtension = documento.getNombreDocumento()
                        .replaceAll("(?i)\\.pdf$", "");
                String path = fecha + "/" + carpeta + "/" + nombreSinExtension + "_signed.pdf";
                storagePort.almacenarBytes(documento.getArchivo(), path, "application/pdf");
                log.info("FirDocfirmadoUseCase: PDF subido a MinIO en path: {}", path);
            } catch (Exception e) {
                log.warn("FirDocfirmadoUseCase: error al subir PDF a MinIO: {}", e.getMessage());
            }
        }

        return guardado;
    }
}
