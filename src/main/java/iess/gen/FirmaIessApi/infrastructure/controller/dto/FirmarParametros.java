/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.controller.dto;

import lombok.*;

/**
 * DTO con los parÃ¡metros de posicionamiento de la firma digital en el PDF.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FirmarParametros {

    /** PosiciÃ³n horizontal de la firma en el PDF. */
    private String llx;

    /** PosiciÃ³n vertical de la firma en el PDF. */
    private String lly;

    /** PÃ¡gina donde se estampa la firma. */
    private String pagina;

    /** Tipo de estampado: QR, FIRMA, etc. */
    private String tipoEstampado;
}
