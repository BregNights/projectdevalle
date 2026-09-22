package br.com.senac.projectdevalle.ordering.domain.order;

// Quem executa uma ação no pedido. SYSTEM representa rotinas automáticas (ex.: confirmação de entrega por prazo).
public enum OrderParty {
    RESTAURANT,
    PRODUCER,
    SYSTEM;

    public OrderParty counterpart() {
        return switch (this) {
            case RESTAURANT -> PRODUCER;
            case PRODUCER -> RESTAURANT;
            case SYSTEM -> SYSTEM;
        };
    }
}
