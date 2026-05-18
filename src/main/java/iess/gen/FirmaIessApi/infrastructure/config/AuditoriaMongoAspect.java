/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.config;

import iess.gen.FirmaIessApi.infrastructure.persistence.mongo.AuditoriaDocument;
import iess.gen.FirmaIessApi.infrastructure.persistence.mongo.AuditoriaMongoRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <b> Aspecto de auditorÃ­a MongoDB para Spring Boot.
 * Intercepta los mÃ©todos crear() de los casos de uso de FirDocfirmado
 * y escribe el documento de auditorÃ­a en MongoDB despuÃ©s del commit exitoso.
 * Solo se activa cuando MONGO_ENABLED=true en el .env.
 * Si MongoDB no estÃ¡ disponible, la auditorÃ­a falla silenciosamente.
 * La transacciÃ³n de negocio NO se revierte. </b>
 *
 * @author Juan Carlos EstÃ©vez Hidalgo
 *
 * @version Revision: 2.0
 *          <p>
 *          [Author: Juan Carlos EstÃ©vez Hidalgo , Date: 14 may 2026]
 *          </p>
 */
@Slf4j
@Aspect
@Component
@ConditionalOnProperty(name = "MONGO_ENABLED", havingValue = "true")
public class AuditoriaMongoAspect {

    /** Repositorio MongoDB para persistencia de documentos de auditorÃ­a. */
    @Autowired(required = false)
    private AuditoriaMongoRepository auditoriaRepository;

    /** Nombre del mÃ³dulo o servicio para poblar aud_program. */
    @Value("${spring.application.name:FirmaIessApi}")
    private String applicationName;

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // INSERT
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * <b> Intercepta crear() de FirDocfirmadoUseCase y FirDocfirmadoDetalleUseCase
     * y registra INSERT en MongoDB post-commit. </b>
     *
     * @param jp        informaciÃ³n del punto de uniÃ³n
     * @param resultado objeto retornado por el mÃ©todo
     */
    @AfterReturning(
            pointcut = "execution(* iess.gen.FirmaIessApi.application.usecase.FirDocfirmadoUseCase.crear(..))"
                     + " || execution(* iess.gen.FirmaIessApi.application.usecase.FirDocfirmadoDetalleUseCase.crear(..))",
            returning = "resultado"
    )
    public void auditarCrear(JoinPoint jp, Object resultado) {
        registrarPostCommit(jp, resultado, "I");
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // LÃ³gica interna
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * <b> Registra el documento de auditorÃ­a en MongoDB despuÃ©s del commit.
     * Nunca lanza excepciÃ³n que revierta la transacciÃ³n de negocio. </b>
     *
     * @param jp        informaciÃ³n del punto de uniÃ³n
     * @param newValues objeto con los nuevos valores
     * @param operacion tipo de operaciÃ³n: I
     */
    private void registrarPostCommit(JoinPoint jp, Object newValues, String operacion) {
        if (auditoriaRepository == null) {
            log.warn("AuditorÃ­a MongoDB: repositorio no disponible. OperaciÃ³n {} no auditada.", operacion);
            return;
        }

        String entidad = jp.getTarget().getClass().getSimpleName()
                .replace("UseCase", "").toUpperCase();
        String coleccion = "AUD_GEN." + entidad;

        Map<String, Object> newValuesMap = newValues != null ? convertirAMapa(newValues) : null;
        List<String> camposFinal = obtenerCamposArgs(jp);

        AuditoriaDocument doc = AuditoriaDocument.builder()
                .audOperation(operacion)
                .audTimestamp(LocalDateTime.now())
                .audDbUser("system")
                .audAppUser(obtenerUsuarioActual())
                .audIpAddress(obtenerIpAddress())
                .audSessionId(obtenerSessionId())
                .audProgram(applicationName + "." + jp.getSignature().getName())
                .audNewValues(newValuesMap)
                .audOldValues(null)
                .audChangedFields(camposFinal)
                .audEntity(entidad)
                .audCollection(coleccion)
                .build();

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    escribirAuditoria(doc, coleccion);
                }
            });
        } else {
            escribirAuditoria(doc, coleccion);
        }
    }

    /**
     * <b> Persiste el documento de auditorÃ­a en MongoDB. </b>
     *
     * @param doc       documento a persistir
     * @param coleccion nombre de la colecciÃ³n para el log
     */
    private void escribirAuditoria(AuditoriaDocument doc, String coleccion) {
        try {
            auditoriaRepository.save(doc);
            log.debug("AuditorÃ­a MongoDB: operaciÃ³n {} registrada en {}",
                    doc.getAudOperation(), coleccion);
        } catch (Exception e) {
            log.warn("AuditorÃ­a MongoDB: error al escribir en {}. Error: {}",
                    coleccion, e.getMessage());
        }
    }

    /**
     * <b> Obtiene el usuario actual. Retorna 'system' sin Spring Security. </b>
     *
     * @return nombre del usuario actual
     */
    private String obtenerUsuarioActual() {
        return "system";
    }

    /**
     * <b> Extrae los tipos de los argumentos del mÃ©todo interceptado. </b>
     *
     * @param jp informaciÃ³n del punto de uniÃ³n
     * @return lista de tipos de argumentos
     */
    private List<String> obtenerCamposArgs(JoinPoint jp) {
        return Arrays.stream(jp.getArgs())
                .filter(arg -> arg != null)
                .map(arg -> arg.getClass().getSimpleName())
                .toList();
    }

    /**
     * <b> Obtiene la IP del cliente desde el HttpServletRequest del hilo actual. </b>
     *
     * @return IP del cliente o null si no hay request activo
     */
    private String obtenerIpAddress() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest request = attrs.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip != null && !ip.isBlank()) {
                return ip.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * <b> Obtiene el ID de sesiÃ³n HTTP del hilo actual. </b>
     *
     * @return ID de sesiÃ³n o null si no hay request activo
     */
    private String obtenerSessionId() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest request = attrs.getRequest();
            var session = request.getSession(false);
            return session != null ? session.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * <b> Convierte un objeto a un mapa de clave-valor. </b>
     *
     * @param objeto objeto a convertir
     * @return mapa de campos y valores
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertirAMapa(Object objeto) {
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper.convertValue(objeto, Map.class);
        } catch (Exception e) {
            return Map.of("value", objeto.toString());
        }
    }
}
