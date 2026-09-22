package br.com.senac.projectdevalle.registration.application.user.port;

import br.com.senac.projectdevalle.shared.domain.vo.Email;

// RF06 — entrega do link de redefinição de senha ao dono da conta (hoje por e-mail; WhatsApp no futuro, RF38).
public interface PasswordResetNotificationPort {

    void sendPasswordResetLink(Email recipient, String resetToken);
}
