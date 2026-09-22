package br.com.senac.projectdevalle.storage.application;

import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;
import br.com.senac.projectdevalle.storage.application.command.UploadFileCommand;
import br.com.senac.projectdevalle.storage.application.port.FileContentStorage;
import br.com.senac.projectdevalle.storage.domain.FilePurpose;
import br.com.senac.projectdevalle.storage.domain.StoredFile;
import br.com.senac.projectdevalle.storage.domain.StoredFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

// RF04/RF07 — envio de fotos de oferta, comprovantes de certificação e documentos do cadastro.
@Service
public class UploadFileService {

    private static final String PRODUCER_ROLE = "PRODUCER";

    private final StoredFileRepository storedFileRepository;
    private final FileContentStorage fileContentStorage;
    private final Clock clock;

    public UploadFileService(StoredFileRepository storedFileRepository, FileContentStorage fileContentStorage,
                             Clock clock) {
        this.storedFileRepository = storedFileRepository;
        this.fileContentStorage = fileContentStorage;
        this.clock = clock;
    }

    @Transactional
    public StoredFile upload(UploadFileCommand command) {
        requireAllowedUploader(command);
        StoredFile storedFile = StoredFile.accept(command.purpose(), command.originalName(), command.content(),
                command.uploaderUserId(), Instant.now(clock));
        fileContentStorage.store(storedFile.id(), command.content());
        return storedFileRepository.save(storedFile);
    }

    // Documentos do cadastro podem ser enviados antes de a conta existir (RF01); fotos e comprovantes de
    // certificação só pelo produtor autenticado.
    private static void requireAllowedUploader(UploadFileCommand command) {
        if (command.purpose() == FilePurpose.SUPPORTING_DOCUMENT) {
            return;
        }
        if (!PRODUCER_ROLE.equals(command.uploaderRole())) {
            throw new BusinessRuleViolationException("Only producers can upload " + command.purpose());
        }
    }
}
