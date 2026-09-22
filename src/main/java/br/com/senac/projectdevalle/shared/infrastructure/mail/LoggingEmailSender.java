package br.com.senac.projectdevalle.shared.infrastructure.mail;

import br.com.senac.projectdevalle.shared.application.port.EmailSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Usado quando não há SMTP configurado. Por padrão registra só que o e-mail NÃO foi enviado; o conteúdo
// (que pode conter links de redefinição de senha) só aparece no log com app.mail.log-content=true (profile dev).
class LoggingEmailSender implements EmailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    private final boolean logContent;

    LoggingEmailSender(boolean logContent) {
        this.logContent = logContent;
    }

    @Override
    public void send(EmailMessage message) {
        if (logContent) {
            log.info("[dev] E-mail não enviado (SMTP não configurado) para {}: {}\n{}", message.to(),
                    message.subject(), message.body());
        } else {
            log.warn("E-mail '{}' não enviado: SMTP não configurado (defina MAIL_HOST)", message.subject());
        }
    }
}
