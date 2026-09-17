package br.com.senac.projectdevalle.registration.application.producer;

import br.com.senac.projectdevalle.registration.application.producer.command.RegisterProducerCommand;
import br.com.senac.projectdevalle.registration.domain.producer.OriginLocation;
import br.com.senac.projectdevalle.registration.domain.producer.Producer;
import br.com.senac.projectdevalle.registration.domain.producer.ProducerRepository;
import br.com.senac.projectdevalle.registration.domain.user.PasswordHasher;
import br.com.senac.projectdevalle.registration.domain.user.Role;
import br.com.senac.projectdevalle.registration.domain.user.User;
import br.com.senac.projectdevalle.registration.domain.user.UserRepository;
import br.com.senac.projectdevalle.shared.application.port.GeocodingPort;
import br.com.senac.projectdevalle.shared.application.port.GeolocationUnavailableException;
import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterProducerService {

    private final UserRepository userRepository;
    private final ProducerRepository producerRepository;
    private final PasswordHasher passwordHasher;
    private final GeocodingPort geocodingPort;

    public RegisterProducerService(UserRepository userRepository, ProducerRepository producerRepository,
                                    PasswordHasher passwordHasher, GeocodingPort geocodingPort) {
        this.userRepository = userRepository;
        this.producerRepository = producerRepository;
        this.passwordHasher = passwordHasher;
        this.geocodingPort = geocodingPort;
    }

    @Transactional
    public RegisterProducerResult register(RegisterProducerCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new BusinessRuleViolationException("Email already registered: " + command.email().value());
        }

        User user = User.register(command.email(), command.rawPassword(), Role.PRODUCER, passwordHasher);
        userRepository.save(user);

        OriginLocation originLocation = resolveOriginLocation(command);

        Producer producer = Producer.register(user.id(), command.name(), command.taxDocument(),
                command.productionType(), originLocation, command.supportingDocuments());
        producerRepository.save(producer);

        return new RegisterProducerResult(producer.id(), user.id(), producer.isGeocodingPending());
    }

    // RF30.1 + RNF11 — falha de geocodificação não impede o cadastro; o produtor fica com
    // geocodingPending=true e a checagem de área de cobertura (RN02) é refeita na aprovação.
    private OriginLocation resolveOriginLocation(RegisterProducerCommand command) {
        try {
            Coordinates coordinates = geocodingPort.geocode(command.originAddress());
            return new OriginLocation(command.originAddress(), coordinates);
        } catch (GeolocationUnavailableException exception) {
            return OriginLocation.withoutCoordinates(command.originAddress());
        }
    }
}
</content>
