package br.com.senac.projectdevalle.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

// JpaSpecificationExecutor porque a busca do catálogo (RF10) combina vários filtros opcionais
// (categoria, produtor, faixa de preço) que não cabem bem em métodos derivados fixos.
interface OfferJpaRepository extends JpaRepository<OfferJpaEntity, UUID>, JpaSpecificationExecutor<OfferJpaEntity> {

    List<OfferJpaEntity> findByProducerId(UUID producerId);
}
