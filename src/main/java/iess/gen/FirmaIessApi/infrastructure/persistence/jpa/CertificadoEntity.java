package iess.gen.FirmaIessApi.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "certificados", schema = "iess")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sistema", nullable = false, unique = true, length = 50)
    private String sistema;

    @Column(name = "certificado", nullable = false, columnDefinition = "TEXT")
    private String certificado;

    @Column(name = "password", nullable = false, length = 500)
    private String password;
}
