package br.com.senac.projectdevalle.registration.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Credenciais do administrador inicial, lidas de ADMIN_BOOTSTRAP_EMAIL/ADMIN_BOOTSTRAP_PASSWORD.
// Deliberadamente sem valor padrão: se não vierem definidas, nenhum admin é criado no boot.
@ConfigurationProperties(prefix = "admin.bootstrap")
public record AdminBootstrapProperties(String email, String password) {

    public boolean isConfigured() {
        return email != null && !email.isBlank() && password != null && !password.isBlank();
    }
}
