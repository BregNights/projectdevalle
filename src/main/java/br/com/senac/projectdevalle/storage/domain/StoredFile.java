package br.com.senac.projectdevalle.storage.domain;

import br.com.senac.projectdevalle.storage.domain.exception.InvalidFileException;

import java.time.Instant;
import java.util.UUID;

// Metadados de um arquivo enviado. O conteúdo fica fora do banco (FileContentStorage), identificado só pelo id —
// nunca pelo nome original, para que nenhum dado do cliente chegue ao caminho no disco.
public record StoredFile(UUID id, FilePurpose purpose, FileType type, long sizeBytes, String originalName,
                         UUID uploadedBy, Instant createdAt) {

    public static final String URL_PREFIX = "/api/v1/files/";

    public static StoredFile accept(FilePurpose purpose, String originalName, byte[] content, UUID uploadedBy,
                                    Instant now) {
        if (purpose == null) {
            throw new InvalidFileException("purpose is required");
        }
        if (content == null || content.length == 0) {
            throw new InvalidFileException("file must not be empty");
        }
        if (content.length > purpose.maxSizeBytes()) {
            throw new InvalidFileException("file exceeds the maximum size of "
                    + (purpose.maxSizeBytes() / (1024 * 1024)) + " MB for " + purpose);
        }
        FileType type = FileType.detect(content)
                .filter(purpose.allowedTypes()::contains)
                .orElseThrow(() -> new InvalidFileException("file type not accepted for " + purpose
                        + "; accepted: " + purpose.allowedTypes()));
        return new StoredFile(UUID.randomUUID(), purpose, type, content.length, sanitize(originalName), uploadedBy,
                now);
    }

    public String url() {
        return URL_PREFIX + id;
    }

    // Arquivos não públicos só podem ser lidos pela administração ou por quem os enviou.
    public boolean isReadableBy(UUID requesterUserId, boolean requesterIsAdministrator) {
        if (purpose.isPubliclyReadable() || requesterIsAdministrator) {
            return true;
        }
        return uploadedBy != null && uploadedBy.equals(requesterUserId);
    }

    private static String sanitize(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "arquivo";
        }
        String name = originalName.replaceAll("[\\\\/]", "_").replaceAll("[\\p{Cntrl}\"]", "").trim();
        return name.length() > 200 ? name.substring(0, 200) : name;
    }
}
