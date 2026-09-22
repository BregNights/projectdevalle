package br.com.senac.projectdevalle.catalog.infrastructure.web;

import br.com.senac.projectdevalle.catalog.application.offer.PauseOfferService;
import br.com.senac.projectdevalle.catalog.application.offer.PublishOfferService;
import br.com.senac.projectdevalle.catalog.application.offer.RemoveOfferService;
import br.com.senac.projectdevalle.catalog.application.offer.ResumeOfferService;
import br.com.senac.projectdevalle.catalog.application.offer.UpdateOfferDetailsService;
import br.com.senac.projectdevalle.catalog.application.offer.UpdateOfferQuantityService;
import br.com.senac.projectdevalle.catalog.application.offer.command.OfferOwnershipCommand;
import br.com.senac.projectdevalle.catalog.application.offer.command.PublishOfferCommand;
import br.com.senac.projectdevalle.catalog.application.offer.command.UpdateOfferDetailsCommand;
import br.com.senac.projectdevalle.catalog.application.offer.command.UpdateOfferQuantityCommand;
import br.com.senac.projectdevalle.catalog.application.port.ProducerDirectoryPort;
import br.com.senac.projectdevalle.catalog.domain.offer.AvailabilityWindow;
import br.com.senac.projectdevalle.catalog.domain.offer.Offer;
import br.com.senac.projectdevalle.catalog.domain.offer.OfferRepository;
import br.com.senac.projectdevalle.catalog.domain.offer.Recurrence;
import br.com.senac.projectdevalle.catalog.infrastructure.web.dto.OfferResponse;
import br.com.senac.projectdevalle.catalog.infrastructure.web.dto.PublishOfferRequest;
import br.com.senac.projectdevalle.catalog.infrastructure.web.dto.UpdateOfferDetailsRequest;
import br.com.senac.projectdevalle.catalog.infrastructure.web.dto.UpdateOfferQuantityRequest;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

// RF07-RF09 — gestão de ofertas pelo próprio produtor autenticado.
@RestController
@RequestMapping("/api/v1/offers")
@PreAuthorize("hasRole('PRODUCER')")
public class OfferController {

    private final PublishOfferService publishOfferService;
    private final UpdateOfferDetailsService updateOfferDetailsService;
    private final UpdateOfferQuantityService updateOfferQuantityService;
    private final PauseOfferService pauseOfferService;
    private final ResumeOfferService resumeOfferService;
    private final RemoveOfferService removeOfferService;
    private final OfferRepository offerRepository;
    private final ProducerDirectoryPort producerDirectoryPort;

    public OfferController(PublishOfferService publishOfferService,
                            UpdateOfferDetailsService updateOfferDetailsService,
                            UpdateOfferQuantityService updateOfferQuantityService,
                            PauseOfferService pauseOfferService, ResumeOfferService resumeOfferService,
                            RemoveOfferService removeOfferService, OfferRepository offerRepository,
                            ProducerDirectoryPort producerDirectoryPort) {
        this.publishOfferService = publishOfferService;
        this.updateOfferDetailsService = updateOfferDetailsService;
        this.updateOfferQuantityService = updateOfferQuantityService;
        this.pauseOfferService = pauseOfferService;
        this.resumeOfferService = resumeOfferService;
        this.removeOfferService = removeOfferService;
        this.offerRepository = offerRepository;
        this.producerDirectoryPort = producerDirectoryPort;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public OfferResponse publish(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PublishOfferRequest request) {
        // RN45 — o tipo informado é respeitado; RECURRING sem dia (ou ONE_TIME com dia) é rejeitado pelo domínio.
        Recurrence recurrence = new Recurrence(request.recurrenceType(), request.recurrenceDayOfWeek());
        AvailabilityWindow availabilityWindow = request.availabilityFrom() != null || request.availabilityUntil() != null
                ? new AvailabilityWindow(request.availabilityFrom(), request.availabilityUntil())
                : null;

        PublishOfferCommand command = new PublishOfferCommand(userId(jwt), request.productName(),
                request.category(), request.unit(), request.price(), request.quantityAvailable(), recurrence,
                availabilityWindow, request.photoUrls());

        Offer offer = publishOfferService.publish(command);
        return OfferResponse.from(offer);
    }

    @GetMapping("/mine")
    public List<OfferResponse> mine(@AuthenticationPrincipal Jwt jwt) {
        UUID producerId = producerDirectoryPort.findProducerIdByUserId(userId(jwt))
                .orElseThrow(() -> new ResourceNotFoundException("No producer registered for current user"));
        return offerRepository.findByProducerId(producerId).stream().map(OfferResponse::from).toList();
    }

    @PutMapping("/{id}")
    public void updateDetails(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                               @Valid @RequestBody UpdateOfferDetailsRequest request) {
        AvailabilityWindow availabilityWindow = request.availabilityFrom() != null
                || request.availabilityUntil() != null
                ? new AvailabilityWindow(request.availabilityFrom(), request.availabilityUntil())
                : null;
        updateOfferDetailsService.update(new UpdateOfferDetailsCommand(id, userId(jwt), request.price(),
                availabilityWindow, request.photoUrls()));
    }

    @PutMapping("/{id}/quantity")
    public void updateQuantity(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                @Valid @RequestBody UpdateOfferQuantityRequest request) {
        updateOfferQuantityService.update(new UpdateOfferQuantityCommand(id, userId(jwt),
                request.quantityAvailable()));
    }

    @PostMapping("/{id}/pause")
    public void pause(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        pauseOfferService.pause(new OfferOwnershipCommand(id, userId(jwt)));
    }

    @PostMapping("/{id}/resume")
    public void resume(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        resumeOfferService.resume(new OfferOwnershipCommand(id, userId(jwt)));
    }

    @PostMapping("/{id}/remove")
    public void remove(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        removeOfferService.remove(new OfferOwnershipCommand(id, userId(jwt)));
    }

    private static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
