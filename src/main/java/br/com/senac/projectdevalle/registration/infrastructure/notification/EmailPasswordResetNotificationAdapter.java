package br.com.senac.projectdevalle.registration.infrastructure.notification;

import br.com.senac.projectdevalle.registration.application.user.port.PasswordResetNotificationPort;
import br.com.senac.projectdevalle.shared.application.port.EmailSenderPort;
import br.com.senac.projectdevalle.shared.application.port.EmailSenderPort.EmailMessage;
import br.com.senac.projectdevalle.shared.domain.vo.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
class EmailPasswordResetNotificationAdapter implements PasswordResetNotificationPort {

    private final EmailSenderPort emailSender;
    private final String frontendUrl;

    EmailPasswordResetNotificationAdapter(EmailSenderPort emailSender,
                                          @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.emailSender = emailSender;
        this.frontendUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1)
                : frontendUrl;
    }

    @Override
    public void sendPasswordResetLink(Email recipient, String resetToken) {
        String link = frontendUrl + "/recuperar-senha?token=" + URLEncoder.encode(resetToken, StandardCharsets.UTF_8);
        String body = """
                Olá,

                Recebemos um pedido para redefinir a senha da sua conta na plataforma.
                Para criar uma nova senha, acesse o link abaixo (válido por 30 minutos):

                %s

                Se você não fez esse pedido, ignore este e-mail: sua senha continua a mesma.
                """.formatted(link);
        emailSender.send(new EmailMessage(recipient.value(), "Redefinição de senha", body));
    }
}
