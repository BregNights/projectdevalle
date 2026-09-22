package br.com.senac.projectdevalle.storage.infrastructure.disk;

import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import br.com.senac.projectdevalle.storage.application.port.FileContentStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.UUID;

// Armazena cada arquivo no disco com o próprio UUID como nome: nada vindo do cliente entra no caminho,
// o que elimina path traversal. Diretório configurável por APP_STORAGE_DIR.
@Component
class LocalDiskFileContentStorage implements FileContentStorage {

    private final Path baseDirectory;

    LocalDiskFileContentStorage(@Value("${app.storage.local-dir:./data/uploads}") String baseDirectory) {
        this.baseDirectory = Path.of(baseDirectory).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.baseDirectory);
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot create upload directory " + this.baseDirectory, exception);
        }
    }

    @Override
    public void store(UUID fileId, byte[] content) {
        try {
            Files.write(pathOf(fileId), content);
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot store file " + fileId, exception);
        }
    }

    @Override
    public byte[] load(UUID fileId) {
        try {
            return Files.readAllBytes(pathOf(fileId));
        } catch (NoSuchFileException exception) {
            throw new ResourceNotFoundException("File content not found: " + fileId);
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot read file " + fileId, exception);
        }
    }

    private Path pathOf(UUID fileId) {
        return baseDirectory.resolve(fileId.toString());
    }
}
