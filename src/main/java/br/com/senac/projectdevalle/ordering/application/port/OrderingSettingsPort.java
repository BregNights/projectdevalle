package br.com.senac.projectdevalle.ordering.application.port;

import java.math.BigDecimal;

public interface OrderingSettingsPort {

    // RN10 — percentual da multa cobrada do restaurante que cancela após o início do preparo.
    BigDecimal cancellationPenaltyPercentage();
}
