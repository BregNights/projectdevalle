package br.com.senac.projectdevalle.registration.domain.producer;

import br.com.senac.projectdevalle.shared.domain.vo.Address;

// RN02 — só produtores dentro da área de cobertura definida pela administração (RF43)
// podem ser listados/aprovados. A política é injetada (não hardcoded no agregado) para
// permitir expansão futura para novas regiões (RNF13) sem alterar o código do domínio.
public interface CoverageAreaPolicy {

    boolean covers(Address address);
}
</content>
