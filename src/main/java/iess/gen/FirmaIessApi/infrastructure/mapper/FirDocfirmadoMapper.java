package iess.gen.FirmaIessApi.infrastructure.mapper;

import iess.gen.FirmaIessApi.model.FirDocfirmado;
import iess.gen.FirmaIessApi.infrastructure.persistence.jpa.FirDocfirmadoEntity;
import iess.gen.FirmaIessApi.infrastructure.persistence.jpa.FirDocfirmadoDetalleEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FirDocfirmadoMapper {

    @Autowired
    private FirDocfirmadoDetalleMapper detalleMapper;

    // =========================
    // DOMAIN â†’ ENTITY
    // =========================
    public FirDocfirmadoEntity toEntity(FirDocfirmado domain) {
        if (domain == null) return null;

        FirDocfirmadoEntity entity = FirDocfirmadoEntity.builder()
                .idDoc(domain.getIdDoc())
                .cedula(domain.getCedula())
                .error(domain.getError())
                .firmasValidas(domain.isFirmasValidas())
                .integridadDocumento(domain.isIntegridadDocumento())
                .nombreDocumento(domain.getNombreDocumento())
                .status(domain.getStatus() != null ? domain.getStatus() : "A")
                .deletedBy(domain.getDeletedBy())
                .deletedAt(domain.getDeletedAt())
                .build();

        // Mapear lista de firmas (detalles)
        if (domain.getFirmas() != null && !domain.getFirmas().isEmpty()) {
            List<FirDocfirmadoDetalleEntity> firmas = domain.getFirmas().stream()
                    .map(detalle -> detalleMapper.toEntity(detalle, entity))
                    .collect(Collectors.toList());
            entity.setFirmas(firmas);
        }

        return entity;
    }

    // =========================
    // ENTITY â†’ DOMAIN
    // =========================
    public FirDocfirmado toDomain(FirDocfirmadoEntity entity) {
        if (entity == null) return null;

        return FirDocfirmado.builder()
                .idDoc(entity.getIdDoc())
                .cedula(entity.getCedula())
                .error(entity.getError())
                .firmasValidas(entity.isFirmasValidas())
                .integridadDocumento(entity.isIntegridadDocumento())
                .nombreDocumento(entity.getNombreDocumento())
                .status(entity.getStatus())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .deletedBy(entity.getDeletedBy())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
}
