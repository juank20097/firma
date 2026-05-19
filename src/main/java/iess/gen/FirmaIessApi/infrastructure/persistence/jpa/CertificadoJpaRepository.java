package iess.gen.FirmaIessApi.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificadoJpaRepository extends JpaRepository<CertificadoEntity, Long> {
    Optional<CertificadoEntity> findBySistema(String sistema);
}
