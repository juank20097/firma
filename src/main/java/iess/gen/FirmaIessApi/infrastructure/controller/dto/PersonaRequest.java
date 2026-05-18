/**
 * Copyright 2026 INSTITUTO ECUATORIANO DE SEGURIDAD SOCIAL - ECUADOR.
 * Todos los derechos reservados.
 */
package iess.gen.FirmaIessApi.infrastructure.controller.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/**
 * <b> DTO de entrada para la creación y actualización de una Persona.
 * Solo expone los campos que el cliente puede enviar.
 * Los campos de auditoría no se incluyen en este objeto. </b>
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
public class PersonaRequest {

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres no pueden superar 100 caracteres")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
    private String apellidos;

    @NotBlank(message = "La cédula es obligatoria")
    @Size(min = 10, max = 13, message = "La cédula debe tener entre 10 y 13 caracteres")
    private String cedula;

    private LocalDate fechaNacimiento;

    @Pattern(regexp = "^[MFO]$", message = "El género debe ser M, F u O")
    private String genero;

    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres")
    private String correo;

    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
    private String telefono;

    @Size(max = 255, message = "La dirección no puede superar 255 caracteres")
    private String direccion;
}
