package br.com.senac.projectdevalle.storage.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface StoredFileJpaRepository extends JpaRepository<StoredFileJpaEntity, UUID> {
}
