/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.controller.dto;

import lombok.*;

/**
 * DTO de respuesta del endpoint POST /iess/firmaec/firmar.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FirmarLoteResponse {

    /** Identificador del lote procesado. */
    private String idLote;

    /** Total de documentos enviados en este bloque. */
    private int totalEnviados;

    /** Total de documentos firmados exitosamente. */
    private int totalFirmados;

    /** Total de documentos con error. */
    private int totalErrores;

    /** Mensaje descriptivo del resultado. */
    private String mensaje;

    /** Estado general: OK, PARCIAL o ERROR. */
    private String estado;
}
