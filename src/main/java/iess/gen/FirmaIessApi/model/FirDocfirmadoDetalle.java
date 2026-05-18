package iess.gen.FirmaIessApi.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * <b>Modelo de dominio que representa el detalle de un documento firmado.</b>
 *
 * <p>
 * Contiene la informaciÃ³n de la firma digital y validaciÃ³n de certificados
 * asociados a un documento firmado dentro del sistema.
 * Este objeto pertenece al nÃºcleo del dominio y no depende de JPA ni de
 * infraestructura.
 * </p>
 *
 * @author Juan Carlos EstÃ©vez Hidalgo
 *
 * @version Revision: 1.0
 *          <p>
 *          [Author: Juan Carlos EstÃ©vez Hidalgo , Date: 13 may 2026]
 *          </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FirDocfirmadoDetalle {

    /** Identificador Ãºnico del detalle del documento firmado. */
    private UUID idDocDet;
    
    /** Identificador de la persona propietaria del documento. */
    private UUID idDoc;

    /** Apellido del firmante. */
    private String apellido;

    /** Cargo del firmante dentro de la instituciÃ³n. */
    private String cargo;

    /** NÃºmero de cÃ©dula del firmante. */
    private String cedula;

    /** Indica si el certificado digital es vÃ¡lido. */
    private boolean certificadoDigitalValido;

    /** Indica si el certificado se encuentra vigente. */
    private boolean certificadoVigente;

    /** Claves de uso del certificado digital. */
    private String clavesUso;

    /** Entidad o persona para la cual fue emitido el certificado. */
    private String emitidoPara;

    /** Entidad que emitiÃ³ el certificado digital. */
    private String emitidoPor;

    /** Entidad certificadora del certificado digital. */
    private String entidadCertificadora;

    /** Fecha en la que se realizÃ³ la firma digital. */
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaFirma;

    /** Fecha en la que el certificado fue revocado (si aplica). */
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaRevocado;

    /** Fecha del sellado de tiempo de la firma. */
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaSellotiempo;

    /** InstituciÃ³n a la que pertenece el firmante. */
    private String institucion;

    /** Indica si la firma mantiene integridad. */
    private boolean integridadFirma;

    /** UbicaciÃ³n geogrÃ¡fica del firmante. */
    private String localizacion;

    /** Nombre del firmante. */
    private String nombre;

    /** RazÃ³n declarada de la firma del documento. */
    private String razonFirma;

    /** Indica si la firma tiene sellado de tiempo. */
    private boolean selladoTiempo;

    /** NÃºmero serial del certificado digital. */
    private String serial;

    /** Fecha desde la cual el certificado es vÃ¡lido. */
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validoDesde;

    /** Fecha hasta la cual el certificado es vÃ¡lido. */
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validoHasta;
    
    /**
     * Estado del registro: A = Activo, I = Inactivo, E = Eliminado lÃ³gico.
     */
    private String status;

    /** Usuario de aplicaciÃ³n que creÃ³ el registro. Propagado desde JWT. */
    private String createdBy;

    /** Fecha y hora exacta de creaciÃ³n con precisiÃ³n de microsegundos. */
    private LocalDateTime createdAt;

    /** Usuario de aplicaciÃ³n que realizÃ³ la Ãºltima modificaciÃ³n. */
    private String updatedBy;

    /** Fecha y hora de la Ãºltima modificaciÃ³n. */
    private LocalDateTime updatedAt;

    /** Usuario que realizÃ³ la eliminaciÃ³n lÃ³gica. NULL si el registro estÃ¡ activo. */
    private String deletedBy;

    /** Fecha y hora de la eliminaciÃ³n lÃ³gica. NULL si el registro estÃ¡ activo. */
    private LocalDateTime deletedAt;
}
