package br.com.senac.projectdevalle.registration.infrastructure.web;

import br.com.senac.projectdevalle.registration.application.restaurant.AddDeliveryAddressService;
import br.com.senac.projectdevalle.registration.application.restaurant.RegisterRestaurantResult;
import br.com.senac.projectdevalle.registration.application.restaurant.RegisterRestaurantService;
import br.com.senac.projectdevalle.registration.application.restaurant.UpdateRestaurantProfileService;
import br.com.senac.projectdevalle.registration.application.restaurant.command.AddDeliveryAddressCommand;
import br.com.senac.projectdevalle.registration.application.restaurant.command.RegisterRestaurantCommand;
import br.com.senac.projectdevalle.registration.application.restaurant.command.UpdateRestaurantProfileCommand;
import br.com.senac.projectdevalle.registration.domain.restaurant.Restaurant;
import br.com.senac.projectdevalle.registration.domain.restaurant.RestaurantRepository;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.AddDeliveryAddressRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RegisterRestaurantRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RestaurantRegistrationResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RestaurantResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.UpdateRestaurantProfileRequest;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import br.com.senac.projectdevalle.shared.domain.vo.Cnpj;
import br.com.senac.projectdevalle.shared.domain.vo.Email;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantController {

    private final RegisterRestaurantService registerRestaurantService;
    private final UpdateRestaurantProfileService updateRestaurantProfileService;
    private final AddDeliveryAddressService addDeliveryAddressService;
    private final RestaurantRepository restaurantRepository;

    public RestaurantController(RegisterRestaurantService registerRestaurantService,
                                 UpdateRestaurantProfileService updateRestaurantProfileService,
                                 AddDeliveryAddressService addDeliveryAddressService,
                                 RestaurantRepository restaurantRepository) {
        this.registerRestaurantService = registerRestaurantService;
        this.updateRestaurantProfileService = updateRestaurantProfileService;
        this.addDeliveryAddressService = addDeliveryAddressService;
        this.restaurantRepository = restaurantRepository;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public RestaurantRegistrationResponse register(@Valid @RequestBody RegisterRestaurantRequest request) {
        RegisterRestaurantCommand command = new RegisterRestaurantCommand(
                new Email(request.email()),
                request.password(),
                request.corporateName(),
                new Cnpj(request.cnpj()),
                request.category(),
                request.contact().toDomain(),
                request.initialAddressLabel(),
                request.initialAddress().toDomain());

        RegisterRestaurantResult result = registerRestaurantService.register(command);
        return new RestaurantRegistrationResponse(result.restaurantId(), result.userId());
    }

    @GetMapping("/{id}")
    public RestaurantResponse findById(@PathVariable UUID id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + id));
        return RestaurantResponse.from(restaurant);
    }

    // Permite ao próprio restaurante autenticado consultar seu cadastro sem conhecer o restaurantId.
    @GetMapping("/me")
    public RestaurantResponse findMine(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Restaurant restaurant = restaurantRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No restaurant registered for current user"));
        return RestaurantResponse.from(restaurant);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}/profile")
    public void updateProfile(@PathVariable UUID id, @Valid @RequestBody UpdateRestaurantProfileRequest request) {
        updateRestaurantProfileService.update(new UpdateRestaurantProfileCommand(id, request.corporateName(),
                request.category(), request.contact().toDomain()));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{id}/delivery-addresses")
    public void addDeliveryAddress(@PathVariable UUID id, @Valid @RequestBody AddDeliveryAddressRequest request) {
        addDeliveryAddressService.add(new AddDeliveryAddressCommand(id, request.label(),
                request.address().toDomain(), request.primary()));
    }
}
