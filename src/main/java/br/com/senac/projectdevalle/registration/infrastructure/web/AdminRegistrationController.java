package br.com.senac.projectdevalle.registration.infrastructure.web;

import br.com.senac.projectdevalle.registration.application.admin.ApproveProducerRegistrationService;
import br.com.senac.projectdevalle.registration.application.admin.ApproveRestaurantRegistrationService;
import br.com.senac.projectdevalle.registration.application.admin.RejectRegistrationService;
import br.com.senac.projectdevalle.registration.application.admin.SuspendRegistrationService;
import br.com.senac.projectdevalle.registration.application.admin.command.ApproveProducerRegistrationCommand;
import br.com.senac.projectdevalle.registration.application.admin.command.ApproveRestaurantRegistrationCommand;
import br.com.senac.projectdevalle.registration.application.admin.command.RejectProducerRegistrationCommand;
import br.com.senac.projectdevalle.registration.application.admin.command.RejectRestaurantRegistrationCommand;
import br.com.senac.projectdevalle.registration.application.admin.command.SuspendProducerRegistrationCommand;
import br.com.senac.projectdevalle.registration.application.admin.command.SuspendRestaurantRegistrationCommand;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RejectRegistrationRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.SuspendRegistrationRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

// RF03/RF40 — fluxo de aprovação/rejeição/suspensão de cadastros, restrito ao Administrador (RNF08).
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class AdminRegistrationController {

    private final ApproveProducerRegistrationService approveProducerRegistrationService;
    private final ApproveRestaurantRegistrationService approveRestaurantRegistrationService;
    private final RejectRegistrationService rejectRegistrationService;
    private final SuspendRegistrationService suspendRegistrationService;

    public AdminRegistrationController(ApproveProducerRegistrationService approveProducerRegistrationService,
                                        ApproveRestaurantRegistrationService approveRestaurantRegistrationService,
                                        RejectRegistrationService rejectRegistrationService,
                                        SuspendRegistrationService suspendRegistrationService) {
        this.approveProducerRegistrationService = approveProducerRegistrationService;
        this.approveRestaurantRegistrationService = approveRestaurantRegistrationService;
        this.rejectRegistrationService = rejectRegistrationService;
        this.suspendRegistrationService = suspendRegistrationService;
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
}
