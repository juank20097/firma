package iess.gen.FirmaIessApi.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import lombok.*;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <b>Entidad JPA que representa la tabla de documentos firmados.</b>
 *
 * <p>
 * Esta clase es parte de la capa de infraestructura y se encarga del
 * mapeo directo a la base de datos.
 * No debe contener lÃ³gica de negocio.
 * </p>
 */
@Entity
@Table(
        name = "FIR_DOCFIRMADOS_T",
        schema = "IESS"
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FirDocfirmadoEntity {

    /** Identificador Ãºnico del documento firmado. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_doc", updatable = false, nullable = false)
    private UUID idDoc;

    /** NÃºmero de cÃ©dula del titular del documento. */
    @Column(name = "cedula", length = 10, nullable = false)
    private String cedula;

    /** Mensaje de error generado durante el proceso de firma. */
    @Column(name = "error", length = 500)
    private String error;

    /** Indica si las firmas son vÃ¡lidas. */
    @Column(name = "firmas_validas")
    private boolean firmasValidas;

    /** Indica si el documento mantiene integridad. */
    @Column(name = "integridad_documento")
    private boolean integridadDocumento;

    /** Nombre original del documento. */
    @Column(name = "nombre_documento", length = 255, nullable = false)
    private String nombreDocumento;

    /** Estado del registro: A, I, E. */
    @Column(name = "status", length = 1, nullable = false)
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

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // RELACIÃ“N CON DETALLES
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @OneToMany(
            mappedBy = "firmados",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<FirDocfirmadoDetalleEntity> firmas = new ArrayList<>();
}
