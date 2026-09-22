package br.com.senac.projectdevalle.catalog.infrastructure.web;

import br.com.senac.projectdevalle.catalog.application.catalog.SearchCatalogService;
import br.com.senac.projectdevalle.catalog.application.catalog.command.SearchCatalogCommand;
import br.com.senac.projectdevalle.catalog.domain.offer.ProductCategory;
import br.com.senac.projectdevalle.catalog.infrastructure.web.dto.CatalogEntryResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// RF10/RF11 — busca do catálogo, disponível para qualquer usuário autenticado (restaurante, produtor ou admin).
@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {

    private final SearchCatalogService searchCatalogService;

    public CatalogController(SearchCatalogService searchCatalogService) {
        this.searchCatalogService = searchCatalogService;
    }

    @GetMapping
    public List<CatalogEntryResponse> search(@AuthenticationPrincipal Jwt jwt,
                                              @RequestParam(required = false) ProductCategory category,
                                              @RequestParam(required = false) UUID producerId,
                                              @RequestParam(required = false) String region,
                                              @RequestParam(required = false) String city,
                                              @RequestParam(required = false) String certificationType,
                                              @RequestParam(required = false) BigDecimal minPrice,
                                              @RequestParam(required = false) BigDecimal maxPrice,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate availableBy) {
        UUID requesterUserId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;
        SearchCatalogCommand command = new SearchCatalogCommand(category, producerId, region, city, certificationType,
                minPrice, maxPrice, availableBy, requesterUserId);
        return searchCatalogService.search(command).stream().map(CatalogEntryResponse::from).toList();
    }

    // RF17 — comparação de ofertas equivalentes (mesmo produto, produtores diferentes).
    @GetMapping("/offers/{offerId}/equivalents")
    public List<CatalogEntryResponse> equivalents(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID offerId) {
        UUID requesterUserId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;
        return searchCatalogService.equivalents(offerId, requesterUserId).stream()
                .map(CatalogEntryResponse::from)
                .toList();
    }
}
