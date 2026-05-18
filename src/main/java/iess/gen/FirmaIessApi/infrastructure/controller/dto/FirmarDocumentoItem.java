/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.controller.dto;

import lombok.*;

/**
 * DTO que representa un documento individual dentro del lote a firmar.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FirmarDocumentoItem {

    /** Nombre del archivo PDF (ej. contrato.pdf). */
    private String nombre;

    /** Contenido del PDF codificado en Base64. */
    private String documento;
}
