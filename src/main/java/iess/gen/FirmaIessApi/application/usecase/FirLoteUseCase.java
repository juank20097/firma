/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.application.usecase;

import iess.gen.FirmaIessApi.infrastructure.config.LoteContextHolder;
import iess.gen.FirmaIessApi.infrastructure.controller.dto.FirmarDocumentoItem;
import iess.gen.FirmaIessApi.infrastructure.controller.dto.FirmarLoteRequest;
import iess.gen.FirmaIessApi.infrastructure.controller.dto.FirmarLoteResponse;
import iess.gen.FirmaIessApi.infrastructure.controller.dto.FirmarParametros;
import iess.gen.FirmaIessApi.infrastructure.persistence.jpa.FirDocfirmadoJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * <b>Caso de uso integrador que orquesta el flujo completo de firma digital.</b>
 *
 * <p>Flujo:</p>
 * <ol>
 *   <li>POST /servicio/documentos â†’ obtiene tokenJwt</li>
 *   <li>POST /api/appfirmardocumentotransversal â†’ firma el lote</li>
 *   <li>Polling en BD esperando callbacks con docs firmados</li>
 *   <li>Devuelve respuesta al sistema externo</li>
 * </ol>
 *
 * @author Juan Carlos EstÃ©vez Hidalgo
 * @version Revision: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FirLoteUseCase {

    /** MÃ¡ximo de documentos permitidos por bloque. Configurable desde el .env. */
    @Value("${firmadigital.max-documentos:20}")
    private int maxDocumentos;

    /** Tiempo de espera entre intentos de polling (ms). */
    private static final long ESPERA_MS = 3000;

    /** Intentos mÃ¡ximos de polling. */
    private static final int MAX_INTENTOS = 10;

    private final FirmaDigitalConfigService configService;
    private final FirDocfirmadoJpaRepository firmadosRepo;
    private final RestTemplate restTemplate;
    private final LoteContextHolder loteContextHolder;

    /** URL base de WildFly. */
    @Value("${firmadigital.url}")
    private String firmadigitalUrl;

    /** RazÃ³n de firma desde el .env. */
    @Value("${firmadigital.razon}")
    private String razon;

    /**
     * <b>Orquesta el flujo completo de firma de un lote de documentos.</b>
     *
     * @param request DTO con idLote, cÃ©dula, certificado, documentos y parÃ¡metros
     * @return respuesta con totales y estado del lote
     */
    public FirmarLoteResponse firmar(FirmarLoteRequest request) {

        // Validaciones bÃ¡sicas
        if (request.getDocumentos() == null || request.getDocumentos().isEmpty()) {
            return buildResponse(request, 0, 0, "Debe enviar al menos un documento.", "ERROR");
        }
        if (request.getDocumentos().size() > maxDocumentos) {
            return buildResponse(request, request.getDocumentos().size(), 0,
                    "MÃ¡ximo " + maxDocumentos + " documentos por bloque.", "ERROR");
        }

        try {
            String sistema  = configService.getSistema();
            String apiKey   = configService.getApiKey();
            String version  = configService.getVersion();

            // Registrar cedula â†’ idLote en el contexto
            loteContextHolder.registrar(request.getCedula(), request.getIdLote());

            // Capturar fecha de inicio del proceso
            java.time.LocalDateTime fechaInicio = java.time.LocalDateTime.now();

            // â”€â”€ PASO 1: Subir documentos â†’ obtener tokenJwt â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            String tokenJwt = obtenerToken(request, sistema, apiKey);
            log.info("FirLoteUseCase: tokenJwt obtenido para lote {}", request.getIdLote());

            // â”€â”€ PASO 2: Firmar con appfirmardocumentotransversal â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            String base64Ctx = construirBase64Contexto(sistema, version);
            String jsonFirma = construirJsonFirma(tokenJwt, sistema, version,
                    resolverParametros(request));
            llamarFirmarTransversal(request.getPkcs12(), request.getPassword(),
                    jsonFirma, base64Ctx);
            log.info("FirLoteUseCase: solicitud de firma enviada para lote {}", request.getIdLote());

            // â”€â”€ PASO 3: Polling â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            List<String> nombresEsperados = request.getDocumentos().stream()
                    .map(FirmarDocumentoItem::getNombre).toList();

            int firmados = esperarDocumentosFirmados(
                    request.getCedula(), nombresEsperados, fechaInicio);

            int errores = request.getDocumentos().size() - firmados;
            String estado = errores == 0 ? "OK" : firmados == 0 ? "ERROR" : "PARCIAL";
            String mensaje = firmados + " documento(s) firmado(s) exitosamente.";

            // Limpiar contexto
            loteContextHolder.limpiar(request.getCedula());

            return buildResponse(request, request.getDocumentos().size(), firmados, mensaje, estado);

        } catch (Exception e) {
            log.error("FirLoteUseCase: error en lote {}: {}", request.getIdLote(), e.getMessage());
            loteContextHolder.limpiar(request.getCedula());
            return buildResponse(request, request.getDocumentos().size(), 0,
                    "Error inesperado: " + e.getMessage(), "ERROR");
        }
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // MÃ©todos privados
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Llama a POST /servicio/documentos y obtiene el tokenJwt.
     */
    private String obtenerToken(FirmarLoteRequest request, String sistema, String apiKey) {
        String url = firmadigitalUrl + "/servicio/documentos";

        StringBuilder docsJson = new StringBuilder("[");
        List<FirmarDocumentoItem> docs = request.getDocumentos();
        for (int i = 0; i < docs.size(); i++) {
            FirmarDocumentoItem doc = docs.get(i);
            docsJson.append("{\"nombre\":\"").append(doc.getNombre())
                    .append("\",\"documento\":\"").append(doc.getDocumento()).append("\"}");
            if (i < docs.size() - 1) docsJson.append(",");
        }
        docsJson.append("]");

        String body = "{\"cedula\":\"" + request.getCedula() + "\","
                + "\"sistema\":\"" + sistema + "\","
                + "\"documentos\":" + docsJson + "}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-KEY", apiKey);

        String respuesta = restTemplate.postForObject(url,
                new HttpEntity<>(body, headers), String.class);

        log.debug("FirLoteUseCase: respuesta tokenJwt: {}", respuesta);
        return respuesta;
    }

    /**
     * Llama a POST /api/appfirmardocumentotransversal.
     */
    private void llamarFirmarTransversal(String pkcs12, String password,
                                          String jsonFirma, String base64Ctx) {
        try {
            String url = firmadigitalUrl + "/api/appfirmardocumentotransversal";
            String body = "pkcs12="  + URLEncoder.encode(pkcs12,    StandardCharsets.UTF_8)
                    + "&password=" + URLEncoder.encode(password,   StandardCharsets.UTF_8)
                    + "&json="     + URLEncoder.encode(jsonFirma,  StandardCharsets.UTF_8)
                    + "&base64="   + URLEncoder.encode(base64Ctx,  StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String respuesta = restTemplate.postForObject(url,
                    new HttpEntity<>(body, headers), String.class);
            log.debug("FirLoteUseCase: respuesta firma: {}", respuesta);
        } catch (Exception e) {
            throw new RuntimeException("Error al llamar appfirmardocumentotransversal: " + e.getMessage(), e);
        }
    }

    /**
     * Polling â€” espera que lleguen los callbacks con los documentos firmados.
     * Filtra por fechaInicio para evitar encontrar registros de firmas anteriores.
     */
    private int esperarDocumentosFirmados(String cedula,
                                           List<String> nombres,
                                           java.time.LocalDateTime fechaInicio) throws InterruptedException {
        for (int i = 0; i < MAX_INTENTOS; i++) {
            Thread.sleep(ESPERA_MS);
            int encontrados = firmadosRepo
                    .findByCedulaAndNombreDocumentoInAndCreatedAtAfter(cedula, nombres, fechaInicio).size();
            log.debug("FirLoteUseCase: polling intento {} â€” {}/{} docs",
                    i + 1, encontrados, nombres.size());
            if (encontrados >= nombres.size()) return encontrados;
        }
        return firmadosRepo
                .findByCedulaAndNombreDocumentoInAndCreatedAtAfter(cedula, nombres, fechaInicio).size();
    }

    /**
     * Construye el JSON de contexto en Base64.
     */
    private String construirBase64Contexto(String sistema, String version) {
        String json = "{\"sistemaOperativo\":\"Linux\","
                + "\"aplicacion\":\"" + sistema + "\","
                + "\"versionApp\":\"" + version + "\","
                + "\"sistema\":\"" + sistema + "\"}";
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Construye el JSON de parÃ¡metros de firma.
     */
    private String construirJsonFirma(String tokenJwt, String sistema,
                                       String version, FirmarParametros params) {
        return "{\"sistema\":\"" + sistema + "\","
                + "\"operacion\":\"FIRMAR\","
                + "\"versionFirmaEC\":\"" + version + "\","
                + "\"tokenJwt\":\"" + tokenJwt + "\","
                + "\"formatoDocumento\":\"pdf\","
                + "\"llx\":\"" + params.getLlx() + "\","
                + "\"lly\":\"" + params.getLly() + "\","
                + "\"pagina\":\"" + params.getPagina() + "\","
                + "\"tipoEstampado\":\"" + params.getTipoEstampado() + "\","
                + "\"razon\":\"" + razon + "\","
                + "\"pre\":false,"
                + "\"des\":false}";
    }

    /**
     * Resuelve los parÃ¡metros globales del lote con valores por defecto si faltan.
     */
    private FirmarParametros resolverParametros(FirmarLoteRequest request) {
        FirmarParametros p = request.getParametros();
        if (p == null) p = new FirmarParametros();
        if (p.getLlx() == null)          p.setLlx("100");
        if (p.getLly() == null)          p.setLly("100");
        if (p.getPagina() == null)       p.setPagina("1");
        if (p.getTipoEstampado() == null) p.setTipoEstampado("QR");
        return p;
    }

    /**
     * Construye el FirmarLoteResponse.
     */
    private FirmarLoteResponse buildResponse(FirmarLoteRequest request,
                                              int enviados, int firmados,
                                              String mensaje, String estado) {
        return FirmarLoteResponse.builder()
                .idLote(request.getIdLote())
                .totalEnviados(enviados)
                .totalFirmados(firmados)
                .totalErrores(enviados - firmados)
                .mensaje(mensaje)
                .estado(estado)
                .build();
    }
}
