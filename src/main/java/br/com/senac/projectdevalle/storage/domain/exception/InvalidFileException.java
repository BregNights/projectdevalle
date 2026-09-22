package br.com.senac.projectdevalle.storage.domain.exception;

// Arquivo recusado por tipo, tamanho ou conteúdo vazio — erro de entrada do cliente (HTTP 400).
public class InvalidFileException extends IllegalArgumentException {

    public InvalidFileException(String message) {
        super(message);
    }
}
