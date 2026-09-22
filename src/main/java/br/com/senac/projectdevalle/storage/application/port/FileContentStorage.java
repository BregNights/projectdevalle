package br.com.senac.projectdevalle.storage.application.port;

import java.util.UUID;

// Onde os bytes dos arquivos ficam guardados (disco local hoje; um bucket S3/GCS pode substituir sem mudar o domínio).
public interface FileContentStorage {

    void store(UUID fileId, byte[] content);

    byte[] load(UUID fileId);
}
