/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.controller.dto;

import lombok.*;

import java.util.List;

/**
 * DTO de respuesta del endpoint GET /iess/firmaec/enlaces/{idLote}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnlacesResponse {

    /** Identificador del lote. */
    private String idLote;

    /** Estado: OK o ERROR. */
    private String estado;

    /** Mensaje descriptivo. */
    private String mensaje;

    /** Lista de enlaces de descarga. */
    private List<EnlaceItem> enlaces;

    /**
     * DTO que representa un enlace de descarga individual.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EnlaceItem {

        /** Nombre del archivo (ZIP o PDF). */
        private String nombre;

        /** URL presignada de descarga directa. */
        private String url;

        /** Horas de validez del link. */
        private int horasExpiracion;
    }
}
