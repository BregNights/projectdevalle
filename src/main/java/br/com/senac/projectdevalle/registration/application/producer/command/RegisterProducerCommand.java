package br.com.senac.projectdevalle.registration.application.producer.command;

import br.com.senac.projectdevalle.registration.domain.producer.ProductionType;
import br.com.senac.projectdevalle.registration.domain.producer.SupportingDocument;
import br.com.senac.projectdevalle.shared.domain.vo.Address;
import br.com.senac.projectdevalle.shared.domain.vo.Email;
import br.com.senac.projectdevalle.shared.domain.vo.TaxDocument;

import java.util.List;

public record RegisterProducerCommand(
        Email email,
        String rawPassword,
        String name,
        TaxDocument taxDocument,
        ProductionType productionType,
        Address originAddress,
        List<SupportingDocument> supportingDocuments
) {
}
</content>
