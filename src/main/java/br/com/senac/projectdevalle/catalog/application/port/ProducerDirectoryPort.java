package br.com.senac.projectdevalle.catalog.application.port;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

// Porta de integração com o Cadastro: o Catálogo nunca depende dos tipos de domínio de registration.* diretamente.
public interface ProducerDirectoryPort {

    Optional<UUID> findProducerIdByUserId(UUID userId);

    Optional<ProducerCatalogInfo> findById(UUID producerId);

    // RN01 — usado antes de publicar/editar/reativar uma oferta.
    boolean isEligibleToOperate(UUID producerId);

    // RN01/RN02 — aprovado e dentro da área de cobertura atual: pode ser listado e vender.
    boolean isListable(UUID producerId);

    // RN01/RN02 — produtores aprovados e dentro da área de cobertura atual. cities/certificationType nulos
    // significam "sem restrição" nessa dimensão; a comparação de municípios ignora acento e caixa.
    Set<UUID> findEligibleProducerIds(Set<String> cities, String certificationType);
}
