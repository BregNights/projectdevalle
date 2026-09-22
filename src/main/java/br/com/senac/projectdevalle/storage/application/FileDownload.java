package br.com.senac.projectdevalle.storage.application;

import br.com.senac.projectdevalle.storage.domain.StoredFile;

public record FileDownload(StoredFile metadata, byte[] content) {
}
