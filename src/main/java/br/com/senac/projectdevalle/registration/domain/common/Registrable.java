package br.com.senac.projectdevalle.registration.domain.common;

// Contrato do fluxo de aprovação (RF03/RF40) compartilhado por Producer e Restaurant.
// A transição de estado e as invariantes de cada agregado ficam implementadas
// nele mesmo — esta interface só padroniza o comportamento exposto aos casos de uso.
// approve()/reactivate() não estão aqui: Producer exige uma CoverageAreaPolicy (RN02) e Restaurant não,
// então cada agregado expõe sua própria assinatura em vez de forçar um contrato artificial.
public interface Registrable {

    void reject(String reason);

    void suspend(String reason);

    void remove(String reason);

    boolean isEligibleToOperate();

    RegistrationStatus status();

    // RN29 — motivo da última rejeição/suspensão/remoção; nulo quando o cadastro está pendente ou aprovado.
    String statusReason();
}
