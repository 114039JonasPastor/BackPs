package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service("emailServiceGmail")
@Slf4j
// @Primary deshabilitado - Render bloquea puertos SMTP (587, 465, 25)
// Usar EmailServiceImpl con Resend API en su lugar
public class EmailServiceGmailImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void enviarCodigoRecuperacion(String email, String codigo) {
        try {
            log.info("📧 Preparando email para: {}", email);

            String htmlBody = "<html><body>" +
                    "<h2>Recuperación de Contraseña</h2>" +
                    "<p>Tu código de recuperación es: <strong>" + codigo + "</strong></p>" +
                    "<p>Este código expira en 15 minutos.</p>" +
                    "</body></html>";

            sendHtml(email, "Código de recuperación de contraseña - Tu Oficio", htmlBody);
            log.info("✅ Código de recuperación enviado exitosamente");

        } catch (Exception e) {
            log.error("❌ Error enviando email de recuperación: ", e);
            throw new RuntimeException("Error enviando email: " + e.getMessage(), e);
        }
    }

    @Override
    public void send(String to, String subject, String body) {
        try {
            log.info("📧 Enviando email de texto plano a: {}", to);
            log.info("📧 Usando Gmail SMTP: {}", fromEmail);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            log.info("✅ Email enviado exitosamente via Gmail SMTP");
            
        } catch (Exception e) {
            log.error("❌ Error enviando email a {}", to);
            log.error("❌ Mensaje: {}", e.getMessage());
            throw new RuntimeException("Error enviando email: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendHtml(String to, String subject, String htmlBody) {
        try {
            log.info("📧 Preparando email HTML para: {}", to);
            log.info("📧 Asunto: {}", subject);
            log.info("📧 Usando Gmail SMTP: {}", fromEmail);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);  // true = es HTML
            
            log.info("📤 Enviando email HTML via Gmail SMTP...");
            mailSender.send(message);
            
            log.info("✅ ¡Email HTML enviado exitosamente a: {}!", to);

        } catch (MessagingException e) {
            log.error("❌ ERROR de Gmail SMTP enviando email HTML a {}", to);
            log.error("❌ Mensaje: {}", e.getMessage());
            log.error("❌ Stack trace:", e);
            throw new RuntimeException("Error enviando email HTML: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ ERROR general enviando email HTML a {}", to);
            log.error("❌ Tipo de error: {}", e.getClass().getName());
            log.error("❌ Mensaje: {}", e.getMessage());
            log.error("❌ Stack trace:", e);
            throw new RuntimeException("Error enviando email HTML: " + e.getMessage(), e);
        }
    }
}
