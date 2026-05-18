package iess.gen.FirmaIessApi.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <b>Modelo de dominio que representa un documento firmado en el sistema.</b>
 *
 * <p>
 * Este objeto forma parte del nÃºcleo del dominio y no depende de frameworks
 * de persistencia ni de infraestructura.
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
public class FirDocfirmado {

    /** Identificador Ãºnico del documento firmado. */
    private UUID idDoc;

    /** NÃºmero de cÃ©dula del titular del documento. */
    private String cedula;

    /** Mensaje de error generado durante el proceso de firma, si aplica. */
    private String error;

    /** Indica si las firmas contenidas en el documento son vÃ¡lidas. */
    private boolean firmasValidas;

    /** Indica si el documento mantiene su integridad despuÃ©s del proceso de firma. */
    private boolean integridadDocumento;

    /** Contenido del PDF firmado en bytes. Solo para deserializaciÃ³n del callback, no se persiste en BD. */
    @com.fasterxml.jackson.annotation.JsonProperty("archivo")
    private byte[] archivo;

    /** Nombre original del documento. */
    private String nombreDocumento;

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
    
    /** Lista de detalles asociados al documento. */
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonProperty("certificado")
    private List<FirDocfirmadoDetalle> firmas = new ArrayList<>();
}
