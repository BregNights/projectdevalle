package br.com.senac.projectdevalle.storage.application;

import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;
import br.com.senac.projectdevalle.storage.application.command.UploadFileCommand;
import br.com.senac.projectdevalle.storage.application.port.FileContentStorage;
import br.com.senac.projectdevalle.storage.domain.FilePurpose;
import br.com.senac.projectdevalle.storage.domain.StoredFile;
import br.com.senac.projectdevalle.storage.domain.StoredFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadFileServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-06-15T10:00:00Z"), ZoneOffset.UTC);
    private static final byte[] PDF = "%PDF-1.7".getBytes(StandardCharsets.US_ASCII);

    @Mock
    private StoredFileRepository storedFileRepository;

    @Mock
    private FileContentStorage fileContentStorage;

    private UploadFileService service;

    @BeforeEach
    void setUp() {
        service = new UploadFileService(storedFileRepository, fileContentStorage, FIXED_CLOCK);
    }

    // RF01 — documento do cadastro pode ser enviado antes de existir conta.
    @Test
    void allowsAnonymousUploadOfRegistrationDocuments() {
        when(storedFileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        StoredFile stored = service.upload(new UploadFileCommand(FilePurpose.SUPPORTING_DOCUMENT, "cpf.pdf", PDF,
                null, null));

        assertThat(stored.uploadedBy()).isNull();
        verify(fileContentStorage).store(stored.id(), PDF);
    }

    @Test
    void onlyProducersCanUploadCertificationProofs() {
        assertThatThrownBy(() -> service.upload(new UploadFileCommand(FilePurpose.CERTIFICATION_PROOF, "c.pdf", PDF,
                UUID.randomUUID(), "RESTAURANT")))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> service.upload(new UploadFileCommand(FilePurpose.CERTIFICATION_PROOF, "c.pdf", PDF,
                null, null)))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(fileContentStorage, never()).store(any(), any());
    }

    @Test
    void producerUploadsCertificationProofRecordingTheUploader() {
        UUID producerUserId = UUID.randomUUID();
        when(storedFileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        StoredFile stored = service.upload(new UploadFileCommand(FilePurpose.CERTIFICATION_PROOF, "c.pdf", PDF,
                producerUserId, "PRODUCER"));

        assertThat(stored.uploadedBy()).isEqualTo(producerUserId);
    }
}
