package br.com.senac.projectdevalle.registration.infrastructure.web;

import br.com.senac.projectdevalle.registration.application.producer.AttachCertificationService;
import br.com.senac.projectdevalle.registration.application.producer.RegisterProducerResult;
import br.com.senac.projectdevalle.registration.application.producer.RegisterProducerService;
import br.com.senac.projectdevalle.registration.application.producer.UpdateBankDetailsService;
import br.com.senac.projectdevalle.registration.application.producer.UpdateDeliveryAreaService;
import br.com.senac.projectdevalle.registration.application.producer.UpdateProducerProfileService;
import br.com.senac.projectdevalle.registration.application.producer.command.AttachCertificationCommand;
import br.com.senac.projectdevalle.registration.application.producer.command.RegisterProducerCommand;
import br.com.senac.projectdevalle.registration.application.producer.command.UpdateBankDetailsCommand;
import br.com.senac.projectdevalle.registration.application.producer.command.UpdateDeliveryAreaCommand;
import br.com.senac.projectdevalle.registration.application.producer.command.UpdateProducerProfileCommand;
import br.com.senac.projectdevalle.registration.domain.producer.Producer;
import br.com.senac.projectdevalle.registration.domain.producer.ProducerRepository;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.AttachCertificationRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.ProducerRegistrationResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.ProducerResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RegisterProducerRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.SupportingDocumentRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.TaxDocumentType;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.UpdateBankDetailsRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.UpdateDeliveryAreaRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.UpdateProducerProfileRequest;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import br.com.senac.projectdevalle.shared.domain.vo.Address;
import br.com.senac.projectdevalle.shared.domain.vo.Cnpj;
import br.com.senac.projectdevalle.shared.domain.vo.Cpf;
import br.com.senac.projectdevalle.shared.domain.vo.Email;
import br.com.senac.projectdevalle.shared.domain.vo.TaxDocument;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

import java.time.Clock;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/producers")
public class ProducerController {

    private final RegisterProducerService registerProducerService;
    private final UpdateProducerProfileService updateProducerProfileService;
    private final AttachCertificationService attachCertificationService;
    private final UpdateBankDetailsService updateBankDetailsService;
    private final UpdateDeliveryAreaService updateDeliveryAreaService;
    private final ProducerRepository producerRepository;
    private final Clock clock;

    public ProducerController(RegisterProducerService registerProducerService,
                               UpdateProducerProfileService updateProducerProfileService,
                               AttachCertificationService attachCertificationService,
                               UpdateBankDetailsService updateBankDetailsService,
                               UpdateDeliveryAreaService updateDeliveryAreaService,
                               ProducerRepository producerRepository, Clock clock) {
        this.registerProducerService = registerProducerService;
        this.updateProducerProfileService = updateProducerProfileService;
        this.attachCertificationService = attachCertificationService;
        this.updateBankDetailsService = updateBankDetailsService;
        this.updateDeliveryAreaService = updateDeliveryAreaService;
        this.producerRepository = producerRepository;
        this.clock = clock;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ProducerRegistrationResponse register(@Valid @RequestBody RegisterProducerRequest request) {
        TaxDocument taxDocument = toTaxDocument(request.taxDocumentType(), request.taxDocumentNumber());
        Address originAddress = request.originAddress().toDomain();

        RegisterProducerCommand command = new RegisterProducerCommand(
                new Email(request.email()),
                request.password(),
                request.name(),
                taxDocument,
                request.productionType(),
                originAddress,
                request.supportingDocuments().stream().map(SupportingDocumentRequest::toDomain).toList());

        RegisterProducerResult result = registerProducerService.register(command);
        return new ProducerRegistrationResponse(result.producerId(), result.userId(), result.geocodingPending());
    }

    @GetMapping("/{id}")
    public ProducerResponse findById(@PathVariable UUID id) {
        Producer producer = producerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producer not found: " + id));
        return ProducerResponse.from(producer, clock);
    }

    // Permite ao próprio produtor autenticado consultar seu cadastro sem conhecer o producerId.
    @GetMapping("/me")
    public ProducerResponse findMine(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Producer producer = producerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No producer registered for current user"));
        return ProducerResponse.from(producer, clock);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}/profile")
    public void updateProfile(@PathVariable UUID id, @Valid @RequestBody UpdateProducerProfileRequest request) {
        updateProducerProfileService.update(new UpdateProducerProfileCommand(id, request.name()));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{id}/certifications")
    public void attachCertification(@PathVariable UUID id, @Valid @RequestBody AttachCertificationRequest request) {
        attachCertificationService.attach(new AttachCertificationCommand(id, request.type(), request.proofUrl(),
                request.validUntil()));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}/bank-details")
    public void updateBankDetails(@PathVariable UUID id, @Valid @RequestBody UpdateBankDetailsRequest request) {
        updateBankDetailsService.update(new UpdateBankDetailsCommand(id, request.toDomain()));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}/delivery-area")
    public void updateDeliveryArea(@PathVariable UUID id, @Valid @RequestBody UpdateDeliveryAreaRequest request) {
        updateDeliveryAreaService.update(new UpdateDeliveryAreaCommand(id, request.toDomain()));
    }

    private static TaxDocument toTaxDocument(TaxDocumentType type, String digits) {
        return type == TaxDocumentType.CPF ? new Cpf(digits) : new Cnpj(digits);
    }
}
