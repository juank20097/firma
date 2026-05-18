/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.controller.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * <b> DTO de salida para la entidad Persona.
 * Expone todos los campos relevantes incluyendo los 7 atributos de auditoría. </b>
 *
 * @author Juan Carlos Estévez Hidalgo
 *
 * @version Revision: 1.0
 *          <p>
 *          [Author: Juan Carlos Estévez Hidalgo , Date: 07 may 2026]
 *          </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaResponse {

    private UUID id;
    private String nombres;
    private String apellidos;
    private String cedula;
    private LocalDate fechaNacimiento;
    private String genero;
    private String correo;
    private String telefono;
    private String direccion;

    /** Estado: A = Activo, I = Inactivo, E = Eliminado lógico. */
    private String status;

    // ── Auditoría estándar PAS-EST-002 sección 6.1 ─────────
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String deletedBy;
    private LocalDateTime deletedAt;
}
