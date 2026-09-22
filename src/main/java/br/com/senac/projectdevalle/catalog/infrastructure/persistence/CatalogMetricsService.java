package br.com.senac.projectdevalle.catalog.infrastructure.persistence;

import br.com.senac.projectdevalle.catalog.domain.offer.OfferStatus;
import br.com.senac.projectdevalle.catalog.domain.offer.ProductCategory;
import br.com.senac.projectdevalle.catalog.infrastructure.web.dto.CatalogMetricsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

// RF42 — indicadores do catálogo para o painel da administração. Assim como RegistrationMetricsService,
// é relatório (contagens), não regra de negócio, então consulta o repositório JPA diretamente.
// Volume transacionado, ticket médio, cancelamentos e tempo de entrega virão com o módulo de pedidos.
@Service
public class CatalogMetricsService {

    private final OfferJpaRepository offerJpaRepository;

    CatalogMetricsService(OfferJpaRepository offerJpaRepository) {
        this.offerJpaRepository = offerJpaRepository;
    }

    @Transactional(readOnly = true)
    public CatalogMetricsResponse collect() {
        Map<String, Long> offersByStatus = new LinkedHashMap<>();
        for (OfferStatus status : OfferStatus.values()) {
            offersByStatus.put(status.name(), offerJpaRepository.countByStatus(status));
        }
        Map<String, Long> activeOffersByCategory = new LinkedHashMap<>();
        for (ProductCategory category : ProductCategory.values()) {
            activeOffersByCategory.put(category.name(), 0L);
        }
        offerJpaRepository.countByCategoryWithStatus(OfferStatus.ACTIVE)
                .forEach(count -> activeOffersByCategory.put(count.getCategory().name(), count.getTotal()));
        return new CatalogMetricsResponse(
                offerJpaRepository.count(),
                offersByStatus,
                activeOffersByCategory,
                offerJpaRepository.countDistinctProducersWithStatus(OfferStatus.ACTIVE));
    }
}
