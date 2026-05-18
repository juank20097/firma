/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.controller.dto;

import lombok.*;

/**
 * DTO de respuesta del endpoint POST /iess/firmaec/empaquetar/{idLote}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpaquetarResponse {

    /** Identificador del lote empaquetado. */
    private String idLote;

    /** Estado: OK o ERROR. */
    private String estado;

    /** Total de ZIPs generados. */
    private int totalZips;

    /** Total de documentos empaquetados. */
    private int totalDocumentos;

    /** Mensaje descriptivo. */
    private String mensaje;
}
