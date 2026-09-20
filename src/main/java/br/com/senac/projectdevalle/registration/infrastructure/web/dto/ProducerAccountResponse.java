package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import br.com.senac.projectdevalle.registration.domain.producer.Producer;
import br.com.senac.projectdevalle.shared.domain.vo.Address;

import java.util.List;
import java.util.UUID;

// Visão detalhada do próprio cadastro (/me/account) — nunca exposta a terceiros, por isso
// inclui dados sensíveis (dados bancários) que ProducerResponse (público/admin) não expõe.
public record ProducerAccountResponse(
        UUID id,
        String name,
        String taxDocumentNumber,
        Address originAddress,
        boolean geocodingPending,
        BankDetailsResponse bankDetails,
        List<String> deliveryAreaMunicipalities
) {

    public static ProducerAccountResponse from(Producer producer) {
        return new ProducerAccountResponse(
                producer.id(),
                producer.name(),
                producer.taxDocument().digits(),
                producer.originLocation().address(),
                producer.isGeocodingPending(),
                BankDetailsResponse.from(producer.bankDetails()),
                producer.deliveryArea().municipalities());
    }
}
