package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public void enviarCodigoRecuperacion(String email, String codigo) {
        try {
            logger.info("Preparando email para: {}", email);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Código de recuperación de contraseña");
            message.setText("Tu código de recuperación es: " + codigo +
                    "\nEste código expira en 15 minutos.");

            logger.info("Enviando email...");
            mailSender.send(message);
            logger.info("Email enviado exitosamente");

        } catch (Exception e) {
            logger.error("Error enviando email: ", e);
            throw new RuntimeException("Error enviando email: " + e.getMessage(), e);
        }
    }

    @Override
    public void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    @Override
    public void sendHtml(String to, String subject, String htmlBody) {
        try {
            logger.info("📧 Preparando email HTML para: {}", to);
            logger.info("📧 Asunto: {}", subject);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String fromEmail = "tuoficiopracticasupervisada@gmail.com";
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indica que es HTML

            logger.info("📧 Configuración SMTP:");
            logger.info("  - From: {}", fromEmail);
            logger.info("  - To: {}", to);
            logger.info("  - Mail Host: {}", System.getProperty("spring.mail.host", "smtp.gmail.com"));
            logger.info("  - Mail Port: {}", System.getProperty("spring.mail.port", "587"));
            
            logger.info("📤 Enviando email HTML...");
            mailSender.send(mimeMessage);
            logger.info("✅ ¡Email HTML enviado exitosamente a: {}!", to);

        } catch (MessagingException e) {
            logger.error("❌ ERROR MessagingException enviando email HTML a {}", to);
            logger.error("❌ Mensaje: {}", e.getMessage());
            logger.error("❌ Causa: {}", e.getCause() != null ? e.getCause().getMessage() : "Sin causa especificada");
            logger.error("❌ Stack trace completo:", e);
            throw new RuntimeException("Error enviando email HTML: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("❌ ERROR general enviando email HTML a {}", to);
            logger.error("❌ Tipo de error: {}", e.getClass().getName());
            logger.error("❌ Mensaje: {}", e.getMessage());
            logger.error("❌ Stack trace completo:", e);
            throw new RuntimeException("Error enviando email HTML: " + e.getMessage(), e);
        }
    }
}
