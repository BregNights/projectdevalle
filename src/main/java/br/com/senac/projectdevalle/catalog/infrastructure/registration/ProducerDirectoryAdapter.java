package br.com.senac.projectdevalle.catalog.infrastructure.registration;

import br.com.senac.projectdevalle.catalog.application.port.ProducerCatalogInfo;
import br.com.senac.projectdevalle.catalog.application.port.ProducerDirectoryPort;
import br.com.senac.projectdevalle.registration.domain.common.RegistrationStatus;
import br.com.senac.projectdevalle.registration.domain.producer.Producer;
import br.com.senac.projectdevalle.registration.domain.producer.ProducerRepository;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class ProducerDirectoryAdapter implements ProducerDirectoryPort {

    private final ProducerRepository producerRepository;
    private final Clock clock;

    public ProducerDirectoryAdapter(ProducerRepository producerRepository, Clock clock) {
        this.producerRepository = producerRepository;
        this.clock = clock;
    }

    @Override
    public Optional<UUID> findProducerIdByUserId(UUID userId) {
        return producerRepository.findByUserId(userId).map(Producer::id);
    }

    @Override
    public Optional<ProducerCatalogInfo> findById(UUID producerId) {
        return producerRepository.findById(producerId).map(this::toCatalogInfo);
    }

    @Override
    public boolean isEligibleToOperate(UUID producerId) {
        return producerRepository.findById(producerId)
                .map(Producer::isEligibleToOperate)
                .orElse(false);
    }

    @Override
    public Set<UUID> findEligibleProducerIds(String city, String certificationType) {
        Set<UUID> result = new HashSet<>();
        for (Producer producer : producerRepository.findByStatus(RegistrationStatus.APPROVED)) {
            if (city != null && !city.equalsIgnoreCase(producer.originLocation().address().city())) {
                continue;
            }
            if (certificationType != null && producer.visibleCertifications(clock).stream()
                    .noneMatch(certification -> certification.type().name().equalsIgnoreCase(certificationType))) {
                continue;
            }
            result.add(producer.id());
        }
        return result;
    }

    private ProducerCatalogInfo toCatalogInfo(Producer producer) {
        return new ProducerCatalogInfo(
                producer.id(),
                producer.name(),
                producer.originLocation().address().city(),
                producer.originLocation().coordinates(),
                producer.visibleCertifications(clock).stream().map(certification -> certification.type().name()).toList());
    }
}
