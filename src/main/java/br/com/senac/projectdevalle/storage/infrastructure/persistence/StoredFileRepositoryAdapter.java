package br.com.senac.projectdevalle.storage.infrastructure.persistence;

import br.com.senac.projectdevalle.storage.domain.StoredFile;
import br.com.senac.projectdevalle.storage.domain.StoredFileRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
class StoredFileRepositoryAdapter implements StoredFileRepository {

    private final StoredFileJpaRepository jpaRepository;

    StoredFileRepositoryAdapter(StoredFileJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public StoredFile save(StoredFile storedFile) {
        return toDomain(jpaRepository.save(StoredFileJpaEntity.builder()
                .id(storedFile.id())
                .purpose(storedFile.purpose())
                .fileType(storedFile.type())
                .sizeBytes(storedFile.sizeBytes())
                .originalName(storedFile.originalName())
                .uploadedBy(storedFile.uploadedBy())
                .createdAt(storedFile.createdAt())
                .build()));
    }

    @Override
    public Optional<StoredFile> findById(UUID id) {
        return jpaRepository.findById(id).map(StoredFileRepositoryAdapter::toDomain);
    }

    private static StoredFile toDomain(StoredFileJpaEntity entity) {
        return new StoredFile(entity.getId(), entity.getPurpose(), entity.getFileType(), entity.getSizeBytes(),
                entity.getOriginalName(), entity.getUploadedBy(), entity.getCreatedAt());
    }
}
