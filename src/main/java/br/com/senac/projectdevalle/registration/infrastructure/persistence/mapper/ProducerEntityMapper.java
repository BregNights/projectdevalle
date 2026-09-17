package br.com.senac.projectdevalle.registration.infrastructure.persistence.mapper;

import br.com.senac.projectdevalle.registration.domain.producer.BankDetails;
import br.com.senac.projectdevalle.registration.domain.producer.Certification;
import br.com.senac.projectdevalle.registration.domain.producer.DeliveryArea;
import br.com.senac.projectdevalle.registration.domain.producer.OriginLocation;
import br.com.senac.projectdevalle.registration.domain.producer.Producer;
import br.com.senac.projectdevalle.registration.domain.producer.SupportingDocument;
import br.com.senac.projectdevalle.registration.infrastructure.persistence.ProducerCertificationJpaEntity;
import br.com.senac.projectdevalle.registration.infrastructure.persistence.ProducerJpaEntity;
import br.com.senac.projectdevalle.registration.infrastructure.persistence.ProducerSupportingDocumentJpaEntity;
import br.com.senac.projectdevalle.shared.domain.vo.Address;
import br.com.senac.projectdevalle.shared.domain.vo.Cnpj;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;
import br.com.senac.projectdevalle.shared.domain.vo.Cpf;
import br.com.senac.projectdevalle.shared.domain.vo.TaxDocument;
import org.springframework.stereotype.Component;

import java.util.List;

// Mapeamento manual: Producer é um agregado com invariantes e construtor privado
// (reconstitute), não um bean simples — não se presta ao mapeamento automático do MapStruct.
@Component
public class ProducerEntityMapper {

    public ProducerJpaEntity toEntity(Producer producer) {
        Address address = producer.originLocation().address();
        Coordinates coordinates = producer.originLocation().coordinates();
        BankDetails bankDetails = producer.bankDetails();

        ProducerJpaEntity entity = ProducerJpaEntity.builder()
                .id(producer.id())
                .userId(producer.userId())
                .name(producer.name())
                .documentType(producer.taxDocument() instanceof Cpf ? "CPF" : "CNPJ")
                .documentNumber(producer.taxDocument().digits())
                .productionType(producer.productionType())
                .originStreet(address.street())
                .originNumber(address.number())
                .originNeighborhood(address.neighborhood())
                .originCity(address.city())
                .originState(address.state())
                .originZipCode(address.zipCode())
                .originComplement(address.complement())
                .originLatitude(coordinates != null ? coordinates.latitude() : null)
                .originLongitude(coordinates != null ? coordinates.longitude() : null)
                .geocodingPending(producer.isGeocodingPending())
                .bankName(bankDetails != null ? bankDetails.bankName() : null)
                .bankAgency(bankDetails != null ? bankDetails.agency() : null)
                .bankAccount(bankDetails != null ? bankDetails.account() : null)
                .bankAccountType(bankDetails != null ? bankDetails.accountType() : null)
                .bankAccountHolder(bankDetails != null ? bankDetails.accountHolder() : null)
                .deliveryArea(producer.deliveryArea().municipalities())
                .status(producer.status())
                .build();

        entity.setSupportingDocuments(producer.supportingDocuments().stream()
                .map(document -> ProducerSupportingDocumentJpaEntity.builder()
                        .id(document.id())
                        .producer(entity)
                        .documentType(document.type())
                        .documentNumber(document.documentNumber())
                        .fileUrl(document.fileUrl())
                        .build())
                .toList());

        entity.setCertifications(producer.certifications().stream()
                .map(certification -> ProducerCertificationJpaEntity.builder()
                        .id(certification.id())
                        .producer(entity)
                        .type(certification.type())
                        .proofUrl(certification.proofUrl())
                        .validUntil(certification.validUntil())
                        .build())
                .toList());

        return entity;
    }

    public Producer toDomain(ProducerJpaEntity entity) {
        TaxDocument taxDocument = "CPF".equals(entity.getDocumentType())
                ? new Cpf(entity.getDocumentNumber())
                : new Cnpj(entity.getDocumentNumber());

        Address address = new Address(entity.getOriginStreet(), entity.getOriginNumber(),
                entity.getOriginNeighborhood(), entity.getOriginCity(), entity.getOriginState(),
                entity.getOriginZipCode(), entity.getOriginComplement());

        Coordinates coordinates = entity.getOriginLatitude() != null && entity.getOriginLongitude() != null
                ? new Coordinates(entity.getOriginLatitude(), entity.getOriginLongitude())
                : null;

        OriginLocation originLocation = new OriginLocation(address, coordinates);

        List<SupportingDocument> supportingDocuments = entity.getSupportingDocuments().stream()
                .map(document -> new SupportingDocument(document.getId(), document.getDocumentType(),
                        document.getDocumentNumber(), document.getFileUrl()))
                .toList();

        List<Certification> certifications = entity.getCertifications().stream()
                .map(certification -> new Certification(certification.getId(), certification.getType(),
                        certification.getProofUrl(), certification.getValidUntil()))
                .toList();

        BankDetails bankDetails = entity.getBankName() != null
                ? new BankDetails(entity.getBankName(), entity.getBankAgency(), entity.getBankAccount(),
                        entity.getBankAccountType(), entity.getBankAccountHolder())
                : null;

        DeliveryArea deliveryArea = new DeliveryArea(entity.getDeliveryArea());

        return Producer.reconstitute(entity.getId(), entity.getUserId(), entity.getName(), taxDocument,
                entity.getProductionType(), originLocation, supportingDocuments, certifications, bankDetails,
                deliveryArea, entity.getStatus());
    }
}
