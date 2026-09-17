package br.com.senac.projectdevalle.registration.application.user.command;

public record ResetPasswordCommand(String resetToken, String newRawPassword) {
}
