package br.com.senac.projectdevalle.registration.application.producer;

import br.com.senac.projectdevalle.registration.application.producer.command.UpdateOriginAddressCommand;
import br.com.senac.projectdevalle.registration.domain.producer.CoverageAreaPolicy;
import br.com.senac.projectdevalle.registration.domain.producer.OriginLocation;
import br.com.senac.projectdevalle.registration.domain.producer.Producer;
import br.com.senac.projectdevalle.registration.domain.producer.ProducerRepository;
import br.com.senac.projectdevalle.shared.application.port.GeocodingPort;
import br.com.senac.projectdevalle.shared.application.port.GeolocationUnavailableException;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import br.com.senac.projectdevalle.shared.domain.vo.Address;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// RF05 — corrigir o endereço de origem após o cadastro; a geocodificação é refeita
// do mesmo jeito best-effort usado no cadastro (RNF11 — falha nunca bloqueia a operação).
// RN02 — para produtor aprovado, o novo endereço precisa continuar dentro da área de cobertura.
@Service
public class UpdateOriginAddressService {

    private final ProducerRepository producerRepository;
    private final GeocodingPort geocodingPort;
    private final CoverageAreaPolicy coverageAreaPolicy;

    public UpdateOriginAddressService(ProducerRepository producerRepository, GeocodingPort geocodingPort,
                                       CoverageAreaPolicy coverageAreaPolicy) {
        this.producerRepository = producerRepository;
        this.geocodingPort = geocodingPort;
        this.coverageAreaPolicy = coverageAreaPolicy;
    }

    @Transactional
    public boolean update(UpdateOriginAddressCommand command) {
        Producer producer = producerRepository.findByUserId(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("No producer registered for current user"));

        producer.updateOriginAddress(resolveOriginLocation(command.address()), coverageAreaPolicy);
        producerRepository.save(producer);

        return producer.isGeocodingPending();
    }

    private OriginLocation resolveOriginLocation(Address address) {
        try {
            Coordinates coordinates = geocodingPort.geocode(address);
            return new OriginLocation(address, coordinates);
        } catch (GeolocationUnavailableException exception) {
            return OriginLocation.withoutCoordinates(address);
        }
    }
}
