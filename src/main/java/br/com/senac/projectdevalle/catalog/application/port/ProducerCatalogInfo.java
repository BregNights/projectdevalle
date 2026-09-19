package br.com.senac.projectdevalle.catalog.application.port;

import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;

import java.util.List;
import java.util.UUID;

// Projeção somente-leitura do Cadastro para o Catálogo — nunca o tipo de domínio Producer diretamente.
public record ProducerCatalogInfo(UUID producerId, String name, String city, Coordinates coordinates,
                                   List<String> certificationTypes) {
}
