/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.controller.dto;

import lombok.*;

import java.util.List;

/**
 * DTO de entrada para el endpoint POST /iess/firmaec/firmar.
 * Agrupa el identificador de lote, el certificado, la clave
 * y la lista de documentos a firmar (mÃ¡ximo 10 por llamada).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FirmarLoteRequest {

    /**
     * Identificador Ãºnico del lote de firma.
     * Generado por el sistema externo â€” debe ser el mismo
     * para todos los bloques de 10 documentos del mismo proceso.
     */
    private String idLote;

    /** CÃ©dula del firmante. */
    private String cedula;

    /** Certificado .p12 codificado en Base64. */
    private String pkcs12;

    /** ContraseÃ±a del certificado codificada en Base64. */
    private String password;

    /** Lista de documentos a firmar (mÃ¡ximo 10 por llamada). */
    private List<FirmarDocumentoItem> documentos;

    /**
     * ParÃ¡metros globales de firma aplicados a todos los documentos
     * que no tengan parÃ¡metros propios.
     */
    private FirmarParametros parametros;
}
