package br.com.senac.projectdevalle.storage.application.command;

import br.com.senac.projectdevalle.storage.domain.FilePurpose;

import java.util.UUID;

// uploaderUserId/uploaderRole são nulos quando o envio é anônimo (documentos do próprio cadastro, RF01).
public record UploadFileCommand(FilePurpose purpose, String originalName, byte[] content, UUID uploaderUserId,
                                String uploaderRole) {
}
