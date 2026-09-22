package br.com.senac.projectdevalle.catalog.infrastructure.persistence;

import br.com.senac.projectdevalle.catalog.domain.offer.OfferStatus;
import br.com.senac.projectdevalle.catalog.domain.offer.ProductCategory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// JpaSpecificationExecutor porque a busca do catálogo (RF10) combina vários filtros opcionais
// (categoria, produtor, faixa de preço) que não cabem bem em métodos derivados fixos.
interface OfferJpaRepository extends JpaRepository<OfferJpaEntity, UUID>, JpaSpecificationExecutor<OfferJpaEntity> {

    List<OfferJpaEntity> findByProducerId(UUID producerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OfferJpaEntity> findWithLockById(UUID id);

    // RF42 — consultas do painel de indicadores.
    long countByStatus(OfferStatus status);

    @Query("SELECT o.category AS category, COUNT(o) AS total FROM OfferJpaEntity o "
            + "WHERE o.status = :status GROUP BY o.category")
    List<CategoryCount> countByCategoryWithStatus(@Param("status") OfferStatus status);

    @Query("SELECT COUNT(DISTINCT o.producerId) FROM OfferJpaEntity o WHERE o.status = :status")
    long countDistinctProducersWithStatus(@Param("status") OfferStatus status);

    interface CategoryCount {

        ProductCategory getCategory();

        long getTotal();
    }
}
