package br.com.senac.projectdevalle.registration.infrastructure.persistence;

import br.com.senac.projectdevalle.registration.domain.common.RegistrationStatus;
import br.com.senac.projectdevalle.registration.domain.producer.ProductionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ProducerJpaRepository extends JpaRepository<ProducerJpaEntity, UUID> {

    Optional<ProducerJpaEntity> findByUserId(UUID userId);

    List<ProducerJpaEntity> findByStatus(RegistrationStatus status);

    long countByStatus(RegistrationStatus status);

    long countByProductionType(ProductionType productionType);

    long countByGeocodingPendingTrue();

    long countByCreatedAtAfter(Instant instant);

    @Query("select p.originCity as city, count(p) as total from ProducerJpaEntity p "
            + "group by p.originCity order by count(p) desc")
    List<CityCount> countGroupedByOriginCity(Pageable pageable);

    interface CityCount {
        String getCity();

        long getTotal();
    }
}
