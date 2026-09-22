package br.com.senac.projectdevalle.registration.infrastructure.web;

import br.com.senac.projectdevalle.registration.application.producer.AttachCertificationService;
import br.com.senac.projectdevalle.registration.application.producer.RegisterProducerResult;
import br.com.senac.projectdevalle.registration.application.producer.RegisterProducerService;
import br.com.senac.projectdevalle.registration.application.producer.UpdateBankDetailsService;
import br.com.senac.projectdevalle.registration.application.producer.UpdateDeliveryAreaService;
import br.com.senac.projectdevalle.registration.application.producer.UpdateOriginAddressService;
import br.com.senac.projectdevalle.registration.application.producer.UpdateProducerProfileService;
import br.com.senac.projectdevalle.registration.application.producer.command.AttachCertificationCommand;
import br.com.senac.projectdevalle.registration.application.producer.command.RegisterProducerCommand;
import br.com.senac.projectdevalle.registration.application.producer.command.UpdateBankDetailsCommand;
import br.com.senac.projectdevalle.registration.application.producer.command.UpdateDeliveryAreaCommand;
import br.com.senac.projectdevalle.registration.application.producer.command.UpdateOriginAddressCommand;
import br.com.senac.projectdevalle.registration.application.producer.command.UpdateProducerProfileCommand;
import br.com.senac.projectdevalle.registration.domain.producer.Producer;
import br.com.senac.projectdevalle.registration.domain.producer.ProducerRepository;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.AttachCertificationRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.ProducerAccountResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.ProducerRegistrationResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.ProducerResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RegisterProducerRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.SupportingDocumentRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.TaxDocumentType;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.UpdateBankDetailsRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.UpdateDeliveryAreaRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.UpdateOriginAddressRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.UpdateOriginAddressResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.UpdateProducerProfileRequest;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import br.com.senac.projectdevalle.shared.domain.vo.Address;
import br.com.senac.projectdevalle.shared.domain.vo.Cnpj;
import br.com.senac.projectdevalle.shared.domain.vo.Cpf;
import br.com.senac.projectdevalle.shared.domain.vo.Email;
import br.com.senac.projectdevalle.shared.domain.vo.TaxDocument;
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
    private final UpdateOriginAddressService updateOriginAddressService;
    private final ProducerRepository producerRepository;
    private final Clock clock;

    public ProducerController(RegisterProducerService registerProducerService,
                               UpdateProducerProfileService updateProducerProfileService,
                               AttachCertificationService attachCertificationService,
                               UpdateBankDetailsService updateBankDetailsService,
                               UpdateDeliveryAreaService updateDeliveryAreaService,
                               UpdateOriginAddressService updateOriginAddressService,
                               ProducerRepository producerRepository, Clock clock) {
        this.registerProducerService = registerProducerService;
        this.updateProducerProfileService = updateProducerProfileService;
        this.attachCertificationService = attachCertificationService;
        this.updateBankDetailsService = updateBankDetailsService;
        this.updateDeliveryAreaService = updateDeliveryAreaService;
        this.updateOriginAddressService = updateOriginAddressService;
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
        return ProducerResponse.publicView(producer, clock);
    }

    // Permite ao próprio produtor autenticado consultar seu cadastro sem conhecer o producerId.
    @GetMapping("/me")
    public ProducerResponse findMine(@AuthenticationPrincipal Jwt jwt) {
        Producer producer = findOwnProducer(jwt);
        return ProducerResponse.from(producer, clock);
    }

    // RF05 — visão detalhada (dados bancários, área de entrega, endereço de origem) para o
    // próprio produtor editar seu cadastro; nunca exposta a terceiros.
    @PreAuthorize("hasRole('PRODUCER')")
    @GetMapping("/me/account")
    public ProducerAccountResponse findMyAccount(@AuthenticationPrincipal Jwt jwt) {
        return ProducerAccountResponse.from(findOwnProducer(jwt));
    }

    // RF05 — todos os endpoints de edição abaixo resolvem o produtor a partir do usuário autenticado
    // (nunca por um {id} vindo do path), para que ninguém edite o cadastro de outra pessoa.
    @PreAuthorize("hasRole('PRODUCER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/me/profile")
    public void updateProfile(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateProducerProfileRequest request) {
        updateProducerProfileService.update(new UpdateProducerProfileCommand(userId(jwt), request.name()));
    }

    @PreAuthorize("hasRole('PRODUCER')")
    @PutMapping("/me/origin-address")
    public UpdateOriginAddressResponse updateOriginAddress(@AuthenticationPrincipal Jwt jwt,
                                                            @Valid @RequestBody UpdateOriginAddressRequest request) {
        boolean geocodingPending = updateOriginAddressService.update(
                new UpdateOriginAddressCommand(userId(jwt), request.address().toDomain()));
        return new UpdateOriginAddressResponse(geocodingPending);
    }

    @PreAuthorize("hasRole('PRODUCER')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/me/certifications")
    public void attachCertification(@AuthenticationPrincipal Jwt jwt,
                                     @Valid @RequestBody AttachCertificationRequest request) {
        attachCertificationService.attach(new AttachCertificationCommand(userId(jwt), request.type(),
                request.proofUrl(), request.validUntil()));
    }

    @PreAuthorize("hasRole('PRODUCER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/me/bank-details")
    public void updateBankDetails(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateBankDetailsRequest request) {
        updateBankDetailsService.update(new UpdateBankDetailsCommand(userId(jwt), request.toDomain()));
    }

    @PreAuthorize("hasRole('PRODUCER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/me/delivery-area")
    public void updateDeliveryArea(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateDeliveryAreaRequest request) {
        updateDeliveryAreaService.update(new UpdateDeliveryAreaCommand(userId(jwt), request.toDomain()));
    }

    private Producer findOwnProducer(Jwt jwt) {
        return producerRepository.findByUserId(userId(jwt))
                .orElseThrow(() -> new ResourceNotFoundException("No producer registered for current user"));
    }

    private static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private static TaxDocument toTaxDocument(TaxDocumentType type, String digits) {
        return type == TaxDocumentType.CPF ? new Cpf(digits) : new Cnpj(digits);
    }
}
