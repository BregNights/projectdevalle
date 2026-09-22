package br.com.senac.projectdevalle.shared.infrastructure.mail;

import br.com.senac.projectdevalle.shared.application.port.EmailSenderPort;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class EmailConfig {

    @Bean
    public EmailSenderPort emailSender(ObjectProvider<JavaMailSender> mailSender,
                                       @Value("${spring.mail.host:}") String mailHost,
                                       @Value("${app.mail.from:nao-responda@projectdevalle.com.br}") String from,
                                       @Value("${app.mail.log-content:false}") boolean logContent) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (mailHost.isBlank() || sender == null) {
            return new LoggingEmailSender(logContent);
        }
        return new SmtpEmailSender(sender, from);
    }
}
