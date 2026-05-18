package iess.gen.FirmaIessApi.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * <b>Entidad JPA que representa el detalle de un documento firmado.</b>
 *
 * <p>
 * Esta clase pertenece a la capa de infraestructura y mapea la tabla
 * de detalles de firmas digitales en base de datos.
 * </p>
 */
@Entity
@Table(
        name = "FIR_DOCFIRMADOSDET_T",
        schema = "IESS"
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FirDocfirmadoDetalleEntity {

    /** Identificador Ãºnico del detalle del documento firmado. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_docdet", updatable = false, nullable = false)
    private UUID idDocDet;

    /** RelaciÃ³n con el documento firmado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_doc", nullable = false)
    private FirDocfirmadoEntity firmados;

    /** Apellido del firmante. */
    @Column(name = "apellido", length = 100)
    private String apellido;

    /** Cargo del firmante. */
    @Column(name = "cargo", length = 256)
    private String cargo;

    /** CÃ©dula del firmante. */
    @Column(name = "cedula", length = 13, nullable = false)
    private String cedula;

    /** Certificado digital vÃ¡lido. */
    @Column(name = "cer_digvalido")
    private boolean certificadoDigitalValido;

    /** Certificado vigente. */
    @Column(name = "cert_vigente")
    private boolean certificadoVigente;

    /** Claves de uso. */
    @Column(name = "claves_uso", length = 100)
    private String clavesUso;

    /** Emitido para. */
    @Column(name = "emitido_para", length = 256)
    private String emitidoPara;

    /** Emitido por. */
    @Column(name = "emitido_por", length = 256)
    private String emitidoPor;

    /** Entidad certificadora. */
    @Column(name = "ent_certificadora", length = 256)
    private String entidadCertificadora;

    /** Fecha de firma. */
    @Column(name = "fec_firma")
    private LocalDateTime fechaFirma;

    /** Fecha de revocaciÃ³n. */
    @Column(name = "fec_revocado")
    private LocalDateTime fechaRevocado;

    /** Fecha de sellado de tiempo. */
    @Column(name = "fec_sellotiempo")
    private LocalDateTime fechaSellotiempo;

    /** InstituciÃ³n. */
    @Column(name = "institucion", length = 256)
    private String institucion;

    /** Integridad de firma. */
    @Column(name = "integridad")
    private boolean integridadFirma;

    /** LocalizaciÃ³n. */
    @Column(name = "localizacion", length = 256)
    private String localizacion;

    /** Nombre del firmante. */
    @Column(name = "nombre", length = 100)
    private String nombre;

    /** RazÃ³n de firma. */
    @Column(name = "razon_firma", length = 256)
    private String razonFirma;

    /** Sellado de tiempo. */
    @Column(name = "sellado_tiempo")
    private boolean selladoTiempo;

    /** Serial del certificado. */
    @Column(name = "serial")
    private String serial;

    /** Validez desde. */
    @Column(name = "valido_desde")
    private LocalDateTime validoDesde;

    /** Validez hasta. */
    @Column(name = "valido_hasta")
    private LocalDateTime validoHasta;

    /** Estado del registro. */
    @Column(name = "status", length = 1)
    private String status;

    // â”€â”€ AuditorÃ­a â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** Usuario de aplicaciÃ³n que creÃ³ el registro. Gestionado automÃ¡ticamente por Spring Auditing. */
    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 100)
    private String createdBy;

    /** Fecha y hora exacta de creaciÃ³n. Gestionada automÃ¡ticamente por Spring Auditing. */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Usuario que realizÃ³ la Ãºltima modificaciÃ³n. Gestionado automÃ¡ticamente por Spring Auditing. */
    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    /** Fecha y hora de la Ãºltima modificaciÃ³n. Gestionada automÃ¡ticamente por Spring Auditing. */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Usuario que realizÃ³ la eliminaciÃ³n lÃ³gica. NULL si el registro estÃ¡ activo. */
    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    /** Fecha y hora de la eliminaciÃ³n lÃ³gica. NULL si el registro estÃ¡ activo. */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
