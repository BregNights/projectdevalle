package br.com.senac.projectdevalle.storage.domain;

import br.com.senac.projectdevalle.storage.domain.exception.InvalidFileException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoredFileTest {

    static final byte[] PDF = "%PDF-1.7 conteudo".getBytes(StandardCharsets.US_ASCII);
    static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3};
    private static final Instant NOW = Instant.parse("2026-06-15T10:00:00Z");

    @Test
    void detectsTypeFromContentNotFromName() {
        StoredFile file = StoredFile.accept(FilePurpose.SUPPORTING_DOCUMENT, "foto.png", PDF, null, NOW);

        assertThat(file.type()).isEqualTo(FileType.PDF);
        assertThat(file.url()).isEqualTo("/api/v1/files/" + file.id());
    }

    @Test
    void rejectsTypeNotAllowedForThePurpose() {
        assertThatThrownBy(() -> StoredFile.accept(FilePurpose.OFFER_PHOTO, "doc.pdf", PDF, UUID.randomUUID(), NOW))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void rejectsUnknownContentDisguisedWithAnAllowedExtension() {
        byte[] executable = "MZ\u0090\u0000".getBytes(StandardCharsets.ISO_8859_1);

        assertThatThrownBy(() -> StoredFile.accept(FilePurpose.OFFER_PHOTO, "foto.jpg", executable, null, NOW))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void rejectsEmptyAndOversizedFiles() {
        assertThatThrownBy(() -> StoredFile.accept(FilePurpose.OFFER_PHOTO, "x.png", new byte[0], null, NOW))
                .isInstanceOf(InvalidFileException.class);

        byte[] tooBig = new byte[(int) FilePurpose.OFFER_PHOTO.maxSizeBytes() + 1];
        System.arraycopy(PNG, 0, tooBig, 0, PNG.length);
        assertThatThrownBy(() -> StoredFile.accept(FilePurpose.OFFER_PHOTO, "x.png", tooBig, null, NOW))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void sanitizesTheOriginalNameAgainstPathCharacters() {
        StoredFile file = StoredFile.accept(FilePurpose.SUPPORTING_DOCUMENT, "..\\..\\etc/passwd.pdf", PDF, null,
                NOW);

        assertThat(file.originalName()).doesNotContain("/").doesNotContain("\\");
    }

    // RNF06 — documentos só para quem enviou e para a administração; fotos de oferta são públicas.
    @Test
    void privateFilesAreReadableOnlyByUploaderOrAdministrator() {
        UUID uploader = UUID.randomUUID();
        StoredFile document = StoredFile.accept(FilePurpose.CERTIFICATION_PROOF, "c.pdf", PDF, uploader, NOW);
        StoredFile photo = StoredFile.accept(FilePurpose.OFFER_PHOTO, "p.png", PNG, uploader, NOW);

        assertThat(document.isReadableBy(uploader, false)).isTrue();
        assertThat(document.isReadableBy(UUID.randomUUID(), true)).isTrue();
        assertThat(document.isReadableBy(UUID.randomUUID(), false)).isFalse();
        assertThat(document.isReadableBy(null, false)).isFalse();
        assertThat(photo.isReadableBy(null, false)).isTrue();
    }

    @Test
    void anonymousRegistrationDocumentsAreReadableOnlyByAdministrator() {
        StoredFile document = StoredFile.accept(FilePurpose.SUPPORTING_DOCUMENT, "cpf.pdf", PDF, null, NOW);

        assertThat(document.isReadableBy(null, false)).isFalse();
        assertThat(document.isReadableBy(UUID.randomUUID(), true)).isTrue();
    }
}
