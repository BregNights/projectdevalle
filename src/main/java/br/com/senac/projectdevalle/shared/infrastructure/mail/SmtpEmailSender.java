package br.com.senac.projectdevalle.shared.infrastructure.mail;

import br.com.senac.projectdevalle.shared.application.port.EmailSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;

// Envio assíncrono: a resposta HTTP não espera o SMTP (nem revela, pelo tempo de resposta, se a conta existe).
// Falhas de envio são registradas no log e nunca propagadas a quem pediu.
class SmtpEmailSender implements EmailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);

    private final JavaMailSender mailSender;
    private final String from;

    SmtpEmailSender(JavaMailSender mailSender, String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Async
    @Override
    public void send(EmailMessage message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(from);
        mail.setTo(message.to());
        mail.setSubject(message.subject());
        mail.setText(message.body());
        try {
            mailSender.send(mail);
        } catch (MailException exception) {
            log.error("Falha ao enviar e-mail '{}' para {}", message.subject(), message.to(), exception);
        }
    }
}
