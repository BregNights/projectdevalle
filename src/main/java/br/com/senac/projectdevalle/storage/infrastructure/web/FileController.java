package br.com.senac.projectdevalle.storage.infrastructure.web;

import br.com.senac.projectdevalle.storage.application.DownloadFileService;
import br.com.senac.projectdevalle.storage.application.FileDownload;
import br.com.senac.projectdevalle.storage.application.UploadFileService;
import br.com.senac.projectdevalle.storage.application.command.UploadFileCommand;
import br.com.senac.projectdevalle.storage.domain.FilePurpose;
import br.com.senac.projectdevalle.storage.infrastructure.web.dto.StoredFileResponse;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

// RF04/RF07 — upload e leitura de arquivos. A autorização fina (quem envia o quê, quem lê o quê) fica nos
// serviços de aplicação, porque depende da finalidade do arquivo; por isso a rota é liberada no SecurityConfig.
@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final UploadFileService uploadFileService;
    private final DownloadFileService downloadFileService;

    public FileController(UploadFileService uploadFileService, DownloadFileService downloadFileService) {
        this.uploadFileService = uploadFileService;
        this.downloadFileService = downloadFileService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StoredFileResponse upload(@AuthenticationPrincipal Jwt jwt, @RequestParam FilePurpose purpose,
                                     @RequestParam MultipartFile file) throws IOException {
        UploadFileCommand command = new UploadFileCommand(purpose, file.getOriginalFilename(), file.getBytes(),
                userId(jwt), role(jwt));
        return StoredFileResponse.from(uploadFileService.upload(command));
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> download(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        FileDownload download = downloadFileService.download(id, userId(jwt), role(jwt));
        CacheControl cacheControl = download.metadata().purpose().isPubliclyReadable()
                ? CacheControl.maxAge(Duration.ofDays(7)).cachePublic()
                : CacheControl.noStore();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.metadata().type().mediaType()))
                .cacheControl(cacheControl)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(download.metadata().originalName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(download.content());
    }

    private static UUID userId(Jwt jwt) {
        return jwt != null ? UUID.fromString(jwt.getSubject()) : null;
    }

    private static String role(Jwt jwt) {
        return jwt != null ? jwt.getClaimAsString("role") : null;
    }
}
