/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.application.usecase;

import iess.gen.FirmaIessApi.infrastructure.persistence.jpa.SistemaEntity;
import iess.gen.FirmaIessApi.infrastructure.persistence.jpa.SistemaJpaRepository;
import iess.gen.FirmaIessApi.infrastructure.persistence.jpa.VersionEntity;
import iess.gen.FirmaIessApi.infrastructure.persistence.jpa.VersionJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * <b>Servicio que lee la configuraciÃ³n de firmadigital desde las tablas
 * public.sistema y public.version para uso en el flujo de firma.</b>
 *
 * @author Juan Carlos EstÃ©vez Hidalgo
 * @version Revision: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FirmaDigitalConfigService {

    private final SistemaJpaRepository sistemaRepository;
    private final VersionJpaRepository versionRepository;

    /** Nombre del sistema registrado en la tabla public.sistema. */
    @Value("${firmadigital.sistema}")
    private String sistemaNombre;

    /**
     * Obtiene el apikeyrest del sistema para autenticarse contra firmadigital-servicio.
     *
     * @return apikeyrest en texto plano
     */
    public String getApiKey() {
        return sistemaRepository.findByNombre(sistemaNombre)
                .map(SistemaEntity::getApikeyrest)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontrÃ³ el sistema '" + sistemaNombre + "' en la tabla public.sistema"));
    }

    /**
     * Obtiene el nombre del sistema registrado en la tabla public.sistema.
     *
     * @return nombre del sistema
     */
    public String getSistema() {
        return sistemaNombre;
    }

    /**
     * Obtiene la versiÃ³n habilitada de FirmaEC desde la tabla public.version.
     *
     * @return versiÃ³n en formato string (ej. 5.1.0)
     */
    public String getVersion() {
        return versionRepository
                .findFirstByAplicacionAndSistemaOperativoAndStatus(sistemaNombre, "LINUX", true)
                .map(VersionEntity::getVersion)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontrÃ³ versiÃ³n habilitada para '" + sistemaNombre + "' en la tabla public.version"));
    }
}
