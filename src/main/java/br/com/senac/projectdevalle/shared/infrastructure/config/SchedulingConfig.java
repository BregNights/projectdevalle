package br.com.senac.projectdevalle.shared.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// Rotinas periódicas (pedidos recorrentes — RF21; confirmação automática de recebimento — RN14).
// Desligadas no profile de testes, que dispara as rotinas diretamente quando precisa.
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
}
