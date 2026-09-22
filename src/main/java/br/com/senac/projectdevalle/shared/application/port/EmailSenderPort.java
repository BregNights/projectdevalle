package br.com.senac.projectdevalle.shared.application.port;

// RF38 (base) — envio de e-mails transacionais. Implementado por SMTP quando MAIL_HOST está configurado;
// sem SMTP, a mensagem não é enviada e isso fica registrado no log.
public interface EmailSenderPort {

    void send(EmailMessage message);

    record EmailMessage(String to, String subject, String body) {

        public EmailMessage {
            if (to == null || to.isBlank()) {
                throw new IllegalArgumentException("to must not be blank");
            }
        }
    }
}
