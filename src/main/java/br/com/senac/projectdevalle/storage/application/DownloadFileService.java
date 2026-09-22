package br.com.senac.projectdevalle.storage.application;

import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import br.com.senac.projectdevalle.storage.application.port.FileContentStorage;
import br.com.senac.projectdevalle.storage.domain.StoredFile;
import br.com.senac.projectdevalle.storage.domain.StoredFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DownloadFileService {

    private static final String ADMINISTRATOR_ROLE = "ADMINISTRATOR";

    private final StoredFileRepository storedFileRepository;
    private final FileContentStorage fileContentStorage;

    public DownloadFileService(StoredFileRepository storedFileRepository, FileContentStorage fileContentStorage) {
        this.storedFileRepository = storedFileRepository;
        this.fileContentStorage = fileContentStorage;
    }

    // RNF06/RNF23 — arquivo sem permissão é tratado como inexistente, para não revelar que ele existe.
    @Transactional(readOnly = true)
    public FileDownload download(UUID fileId, UUID requesterUserId, String requesterRole) {
        StoredFile storedFile = storedFileRepository.findById(fileId)
                .filter(file -> file.isReadableBy(requesterUserId, ADMINISTRATOR_ROLE.equals(requesterRole)))
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));
        return new FileDownload(storedFile, fileContentStorage.load(storedFile.id()));
    }
}
