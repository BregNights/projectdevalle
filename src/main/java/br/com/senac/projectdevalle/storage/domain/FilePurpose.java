package br.com.senac.projectdevalle.storage.domain;

import java.util.Set;

// RF04/RF07/RF01 — finalidade do arquivo enviado, que define tipos aceitos, tamanho máximo e quem pode ver.
public enum FilePurpose {

    // Fotos de ofertas aparecem no catálogo: são públicas.
    OFFER_PHOTO(Set.of(FileType.JPEG, FileType.PNG, FileType.WEBP), 5L * 1024 * 1024, true),
    // Documentos comprobatórios do cadastro (CPF/CNPJ, DAP/CAF, registro de pesca): dado sensível (RNF06).
    SUPPORTING_DOCUMENT(Set.of(FileType.PDF, FileType.JPEG, FileType.PNG), 10L * 1024 * 1024, false),
    // Comprovantes de certificação (RN03): vistos pela administração e pelo próprio produtor.
    CERTIFICATION_PROOF(Set.of(FileType.PDF, FileType.JPEG, FileType.PNG), 10L * 1024 * 1024, false);

    private final Set<FileType> allowedTypes;
    private final long maxSizeBytes;
    private final boolean publiclyReadable;

    FilePurpose(Set<FileType> allowedTypes, long maxSizeBytes, boolean publiclyReadable) {
        this.allowedTypes = allowedTypes;
        this.maxSizeBytes = maxSizeBytes;
        this.publiclyReadable = publiclyReadable;
    }

    public Set<FileType> allowedTypes() {
        return allowedTypes;
    }

    public long maxSizeBytes() {
        return maxSizeBytes;
    }

    public boolean isPubliclyReadable() {
        return publiclyReadable;
    }
}
