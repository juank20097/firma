/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>Componente que mantiene en memoria el mapeo cedula â†’ idLote.</b>
 *
 * <p>
 * Cuando FirLoteUseCase inicia el proceso de firma registra la cÃ©dula con su idLote.
 * Cuando llega el callback de firmadigital-servicio, FirDocfirmadoUseCase consulta
 * este holder para obtener el idLote y construir el path correcto en MinIO.
 * El registro se elimina automÃ¡ticamente cuando se completa el proceso.
 * </p>
 *
 * @author Juan Carlos EstÃ©vez Hidalgo
 * @version Revision: 1.0
 */
@Slf4j
@Component
public class LoteContextHolder {

    /** Mapa thread-safe de cedula â†’ idLote. */
    private final ConcurrentHashMap<String, String> contexto = new ConcurrentHashMap<>();

    /**
     * Registra la cÃ©dula con su idLote al iniciar el proceso de firma.
     *
     * @param cedula cÃ©dula del firmante
     * @param idLote identificador del lote
     */
    public void registrar(String cedula, String idLote) {
        contexto.put(cedula, idLote);
        log.debug("LoteContextHolder: registrado cedula={} â†’ idLote={}", cedula, idLote);
    }

    /**
     * Obtiene el idLote asociado a la cÃ©dula.
     *
     * @param cedula cÃ©dula del firmante
     * @return idLote o null si no estÃ¡ registrado
     */
    public String obtener(String cedula) {
        return contexto.get(cedula);
    }

    /**
     * Elimina el registro de la cÃ©dula una vez completado el proceso.
     *
     * @param cedula cÃ©dula del firmante
     */
    public void limpiar(String cedula) {
        contexto.remove(cedula);
        log.debug("LoteContextHolder: limpiado registro para cedula={}", cedula);
    }
}
