package br.com.senac.projectdevalle.storage.domain;

import java.util.Optional;
import java.util.UUID;

public interface StoredFileRepository {

    StoredFile save(StoredFile storedFile);

    Optional<StoredFile> findById(UUID id);
}
