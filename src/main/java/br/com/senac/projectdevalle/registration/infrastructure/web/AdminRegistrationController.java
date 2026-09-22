package br.com.senac.projectdevalle.registration.infrastructure.web;

import br.com.senac.projectdevalle.registration.application.admin.ApproveProducerRegistrationService;
import br.com.senac.projectdevalle.registration.application.admin.ApproveRestaurantRegistrationService;
import br.com.senac.projectdevalle.registration.application.admin.ReactivateRegistrationService;
import br.com.senac.projectdevalle.registration.application.admin.RejectRegistrationService;
import br.com.senac.projectdevalle.registration.application.admin.RemoveRegistrationService;
import br.com.senac.projectdevalle.registration.application.admin.SuspendRegistrationService;
import br.com.senac.projectdevalle.registration.application.admin.command.ApproveProducerRegistrationCommand;
import br.com.senac.projectdevalle.registration.application.admin.command.ApproveRestaurantRegistrationCommand;
import br.com.senac.projectdevalle.registration.application.admin.command.ReactivateProducerRegistrationCommand;
import br.com.senac.projectdevalle.registration.application.admin.command.ReactivateRestaurantRegistrationCommand;
import br.com.senac.projectdevalle.registration.application.admin.command.RejectProducerRegistrationCommand;
import br.com.senac.projectdevalle.registration.application.admin.command.RejectRestaurantRegistrationCommand;
import br.com.senac.projectdevalle.registration.application.admin.command.RemoveProducerRegistrationCommand;
import br.com.senac.projectdevalle.registration.application.admin.command.RemoveRestaurantRegistrationCommand;
import br.com.senac.projectdevalle.registration.application.admin.command.SuspendProducerRegistrationCommand;
import br.com.senac.projectdevalle.registration.application.admin.command.SuspendRestaurantRegistrationCommand;
import br.com.senac.projectdevalle.registration.domain.common.RegistrationStatus;
import br.com.senac.projectdevalle.registration.domain.producer.ProducerRepository;
import br.com.senac.projectdevalle.registration.domain.restaurant.RestaurantRepository;
import br.com.senac.projectdevalle.registration.infrastructure.persistence.RegistrationMetricsService;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.AdminMetricsResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.ProducerResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RejectRegistrationRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RemoveRegistrationRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RestaurantResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.SuspendRegistrationRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

// RF03/RF40 — fluxo de aprovação/rejeição/suspensão/reativação/remoção de cadastros, restrito ao Administrador (RNF08).
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class AdminRegistrationController {

    private final ApproveProducerRegistrationService approveProducerRegistrationService;
    private final ApproveRestaurantRegistrationService approveRestaurantRegistrationService;
    private final RejectRegistrationService rejectRegistrationService;
    private final SuspendRegistrationService suspendRegistrationService;
    private final ReactivateRegistrationService reactivateRegistrationService;
    private final RemoveRegistrationService removeRegistrationService;
    private final ProducerRepository producerRepository;
    private final RestaurantRepository restaurantRepository;
    private final RegistrationMetricsService registrationMetricsService;
    private final Clock clock;

    public AdminRegistrationController(ApproveProducerRegistrationService approveProducerRegistrationService,
                                        ApproveRestaurantRegistrationService approveRestaurantRegistrationService,
                                        RejectRegistrationService rejectRegistrationService,
                                        SuspendRegistrationService suspendRegistrationService,
                                        ReactivateRegistrationService reactivateRegistrationService,
                                        RemoveRegistrationService removeRegistrationService,
                                        ProducerRepository producerRepository,
                                        RestaurantRepository restaurantRepository,
                                        RegistrationMetricsService registrationMetricsService,
                                        Clock clock) {
        this.approveProducerRegistrationService = approveProducerRegistrationService;
        this.approveRestaurantRegistrationService = approveRestaurantRegistrationService;
        this.rejectRegistrationService = rejectRegistrationService;
        this.suspendRegistrationService = suspendRegistrationService;
        this.reactivateRegistrationService = reactivateRegistrationService;
        this.removeRegistrationService = removeRegistrationService;
        this.producerRepository = producerRepository;
        this.restaurantRepository = restaurantRepository;
        this.registrationMetricsService = registrationMetricsService;
        this.clock = clock;
    }

    @GetMapping("/metrics")
    public AdminMetricsResponse metrics() {
        return registrationMetricsService.collect();
    }

    // RF40 — lista de cadastros para a fila de aprovação do administrador, filtrável por status.
    @GetMapping("/producers")
    public List<ProducerResponse> listProducers(
            @RequestParam(defaultValue = "PENDING") RegistrationStatus status) {
        return producerRepository.findByStatus(status).stream()
                .map(producer -> ProducerResponse.from(producer, clock))
                .toList();
    }

    @GetMapping("/restaurants")
    public List<RestaurantResponse> listRestaurants(
            @RequestParam(defaultValue = "PENDING") RegistrationStatus status) {
        return restaurantRepository.findByStatus(status).stream()
                .map(RestaurantResponse::from)
                .toList();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/producers/{id}/approve")
    public void approveProducer(@PathVariable UUID id) {
        approveProducerRegistrationService.approve(new ApproveProducerRegistrationCommand(id));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/producers/{id}/reject")
    public void rejectProducer(@PathVariable UUID id, @Valid @RequestBody RejectRegistrationRequest request) {
        rejectRegistrationService.rejectProducer(new RejectProducerRegistrationCommand(id, request.reason()));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/producers/{id}/suspend")
    public void suspendProducer(@PathVariable UUID id, @Valid @RequestBody SuspendRegistrationRequest request) {
        suspendRegistrationService.suspendProducer(new SuspendProducerRegistrationCommand(id, request.reason()));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/restaurants/{id}/approve")
    public void approveRestaurant(@PathVariable UUID id) {
        approveRestaurantRegistrationService.approve(new ApproveRestaurantRegistrationCommand(id));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/restaurants/{id}/reject")
    public void rejectRestaurant(@PathVariable UUID id, @Valid @RequestBody RejectRegistrationRequest request) {
        rejectRegistrationService.rejectRestaurant(new RejectRestaurantRegistrationCommand(id, request.reason()));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/restaurants/{id}/suspend")
    public void suspendRestaurant(@PathVariable UUID id, @Valid @RequestBody SuspendRegistrationRequest request) {
        suspendRegistrationService.suspendRestaurant(new SuspendRestaurantRegistrationCommand(id, request.reason()));
    }

    // RF40 — reativação de cadastro suspenso.
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/producers/{id}/reactivate")
    public void reactivateProducer(@PathVariable UUID id) {
        reactivateRegistrationService.reactivateProducer(new ReactivateProducerRegistrationCommand(id));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/restaurants/{id}/reactivate")
    public void reactivateRestaurant(@PathVariable UUID id) {
        reactivateRegistrationService.reactivateRestaurant(new ReactivateRestaurantRegistrationCommand(id));
    }

    // RF40 — remoção definitiva de cadastro (o usuário vinculado perde o acesso).
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/producers/{id}/remove")
    public void removeProducer(@PathVariable UUID id, @Valid @RequestBody RemoveRegistrationRequest request) {
        removeRegistrationService.removeProducer(new RemoveProducerRegistrationCommand(id, request.reason()));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/restaurants/{id}/remove")
    public void removeRestaurant(@PathVariable UUID id, @Valid @RequestBody RemoveRegistrationRequest request) {
        removeRegistrationService.removeRestaurant(new RemoveRestaurantRegistrationCommand(id, request.reason()));
    }
}
