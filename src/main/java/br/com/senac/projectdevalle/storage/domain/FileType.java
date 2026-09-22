package br.com.senac.projectdevalle.storage.domain;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

// O tipo é detectado pelo conteúdo (assinatura dos primeiros bytes), nunca pela extensão ou pelo
// Content-Type informado pelo cliente — que podem ser forjados.
public enum FileType {

    PDF("application/pdf"),
    JPEG("image/jpeg"),
    PNG("image/png"),
    WEBP("image/webp");

    private static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] RIFF_SIGNATURE = "RIFF".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] WEBP_MARKER = "WEBP".getBytes(StandardCharsets.US_ASCII);

    private final String mediaType;

    FileType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String mediaType() {
        return mediaType;
    }

    public static Optional<FileType> detect(byte[] content) {
        if (content == null) {
            return Optional.empty();
        }
        if (startsWith(content, PDF_SIGNATURE)) {
            return Optional.of(PDF);
        }
        if (startsWith(content, JPEG_SIGNATURE)) {
            return Optional.of(JPEG);
        }
        if (startsWith(content, PNG_SIGNATURE)) {
            return Optional.of(PNG);
        }
        if (content.length >= 12 && startsWith(content, RIFF_SIGNATURE)
                && Arrays.equals(Arrays.copyOfRange(content, 8, 12), WEBP_MARKER)) {
            return Optional.of(WEBP);
        }
        return Optional.empty();
    }

    private static boolean startsWith(byte[] content, byte[] signature) {
        return content.length >= signature.length
                && Arrays.equals(Arrays.copyOfRange(content, 0, signature.length), signature);
    }
}
