package br.com.senac.projectdevalle.registration.infrastructure.web;

import br.com.senac.projectdevalle.registration.application.restaurant.AddDeliveryAddressService;
import br.com.senac.projectdevalle.registration.application.restaurant.RegisterRestaurantResult;
import br.com.senac.projectdevalle.registration.application.restaurant.RegisterRestaurantService;
import br.com.senac.projectdevalle.registration.application.restaurant.RemoveDeliveryAddressService;
import br.com.senac.projectdevalle.registration.application.restaurant.SetPrimaryDeliveryAddressService;
import br.com.senac.projectdevalle.registration.application.restaurant.UpdateRestaurantProfileService;
import br.com.senac.projectdevalle.registration.application.restaurant.command.AddDeliveryAddressCommand;
import br.com.senac.projectdevalle.registration.application.restaurant.command.RegisterRestaurantCommand;
import br.com.senac.projectdevalle.registration.application.restaurant.command.RemoveDeliveryAddressCommand;
import br.com.senac.projectdevalle.registration.application.restaurant.command.SetPrimaryDeliveryAddressCommand;
import br.com.senac.projectdevalle.registration.application.restaurant.command.UpdateRestaurantProfileCommand;
import br.com.senac.projectdevalle.registration.domain.restaurant.DeliveryAddress;
import br.com.senac.projectdevalle.registration.domain.restaurant.Restaurant;
import br.com.senac.projectdevalle.registration.domain.restaurant.RestaurantRepository;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.AddDeliveryAddressRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.DeliveryAddressResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RegisterRestaurantRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RestaurantAccountResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RestaurantRegistrationResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RestaurantResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.UpdateRestaurantProfileRequest;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import br.com.senac.projectdevalle.shared.domain.vo.Cnpj;
import br.com.senac.projectdevalle.shared.domain.vo.Email;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
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

@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantController {

    private final RegisterRestaurantService registerRestaurantService;
    private final UpdateRestaurantProfileService updateRestaurantProfileService;
    private final AddDeliveryAddressService addDeliveryAddressService;
    private final SetPrimaryDeliveryAddressService setPrimaryDeliveryAddressService;
    private final RemoveDeliveryAddressService removeDeliveryAddressService;
    private final RestaurantRepository restaurantRepository;

    public RestaurantController(RegisterRestaurantService registerRestaurantService,
                                 UpdateRestaurantProfileService updateRestaurantProfileService,
                                 AddDeliveryAddressService addDeliveryAddressService,
                                 SetPrimaryDeliveryAddressService setPrimaryDeliveryAddressService,
                                 RemoveDeliveryAddressService removeDeliveryAddressService,
                                 RestaurantRepository restaurantRepository) {
        this.registerRestaurantService = registerRestaurantService;
        this.updateRestaurantProfileService = updateRestaurantProfileService;
        this.addDeliveryAddressService = addDeliveryAddressService;
        this.setPrimaryDeliveryAddressService = setPrimaryDeliveryAddressService;
        this.removeDeliveryAddressService = removeDeliveryAddressService;
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
        return RestaurantResponse.publicView(restaurant);
    }

    // Permite ao próprio restaurante autenticado consultar seu cadastro sem conhecer o restaurantId.
    @GetMapping("/me")
    public RestaurantResponse findMine(@AuthenticationPrincipal Jwt jwt) {
        return RestaurantResponse.from(findOwnRestaurant(jwt));
    }

    // RF05 — visão detalhada (contato completo, CNPJ, endereços de entrega) para o próprio
    // restaurante editar seu cadastro; nunca exposta a terceiros.
    @PreAuthorize("hasRole('RESTAURANT')")
    @GetMapping("/me/account")
    public RestaurantAccountResponse findMyAccount(@AuthenticationPrincipal Jwt jwt) {
        return RestaurantAccountResponse.from(findOwnRestaurant(jwt));
    }

    // RF05 — todos os endpoints de edição abaixo resolvem o restaurante a partir do usuário autenticado
    // (nunca por um {id} vindo do path), para que ninguém edite o cadastro de outra pessoa.
    @PreAuthorize("hasRole('RESTAURANT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/me/profile")
    public void updateProfile(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateRestaurantProfileRequest request) {
        updateRestaurantProfileService.update(new UpdateRestaurantProfileCommand(userId(jwt), request.corporateName(),
                request.category(), request.contact().toDomain()));
    }

    @PreAuthorize("hasRole('RESTAURANT')")
    @GetMapping("/me/delivery-addresses")
    public List<DeliveryAddressResponse> listDeliveryAddresses(@AuthenticationPrincipal Jwt jwt) {
        return findOwnRestaurant(jwt).deliveryAddresses().stream().map(DeliveryAddressResponse::from).toList();
    }

    @PreAuthorize("hasRole('RESTAURANT')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/me/delivery-addresses")
    public DeliveryAddressResponse addDeliveryAddress(@AuthenticationPrincipal Jwt jwt,
                                                        @Valid @RequestBody AddDeliveryAddressRequest request) {
        DeliveryAddress deliveryAddress = addDeliveryAddressService.add(new AddDeliveryAddressCommand(userId(jwt),
                request.label(), request.address().toDomain(), request.primary()));
        return DeliveryAddressResponse.from(deliveryAddress);
    }

    @PreAuthorize("hasRole('RESTAURANT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/me/delivery-addresses/{addressId}/primary")
    public void setPrimaryDeliveryAddress(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID addressId) {
        setPrimaryDeliveryAddressService.setPrimary(new SetPrimaryDeliveryAddressCommand(userId(jwt), addressId));
    }

    @PreAuthorize("hasRole('RESTAURANT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/me/delivery-addresses/{addressId}")
    public void removeDeliveryAddress(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID addressId) {
        removeDeliveryAddressService.remove(new RemoveDeliveryAddressCommand(userId(jwt), addressId));
    }

    private Restaurant findOwnRestaurant(Jwt jwt) {
        return restaurantRepository.findByUserId(userId(jwt))
                .orElseThrow(() -> new ResourceNotFoundException("No restaurant registered for current user"));
    }

    private static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
