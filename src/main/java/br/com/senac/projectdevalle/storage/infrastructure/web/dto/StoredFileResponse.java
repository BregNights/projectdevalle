package br.com.senac.projectdevalle.storage.infrastructure.web.dto;

import br.com.senac.projectdevalle.storage.domain.FilePurpose;
import br.com.senac.projectdevalle.storage.domain.StoredFile;

import java.util.UUID;

public record StoredFileResponse(UUID id, String url, FilePurpose purpose, String contentType, long sizeBytes,
                                 String originalName) {

    public static StoredFileResponse from(StoredFile storedFile) {
        return new StoredFileResponse(storedFile.id(), storedFile.url(), storedFile.purpose(),
                storedFile.type().mediaType(), storedFile.sizeBytes(), storedFile.originalName());
    }
}
