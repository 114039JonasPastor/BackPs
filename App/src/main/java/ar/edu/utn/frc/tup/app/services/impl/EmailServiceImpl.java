package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.services.EmailService;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("emailServiceResend")
@Slf4j
// Deshabilitado en favor de SendGrid
public class EmailServiceImpl implements EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.from.email:TuOficio <noreply@tuoficio.com>}")
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
            
            Resend resend = new Resend(resendApiKey);
            
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject(subject)
                    .text(body)
                    .build();
            
            CreateEmailResponse response = resend.emails().send(params);
            log.info("✅ Email enviado exitosamente. ID: {}", response.getId());
            
        } catch (ResendException e) {
            log.error("❌ Error de Resend enviando email a {}", to);
            log.error("❌ Mensaje: {}", e.getMessage());
            throw new RuntimeException("Error enviando email: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Error general enviando email a {}", to);
            log.error("❌ Mensaje: {}", e.getMessage());
            throw new RuntimeException("Error enviando email: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendHtml(String to, String subject, String htmlBody) {
        try {
            log.info("📧 Preparando email HTML para: {}", to);
            log.info("📧 Asunto: {}", subject);
            log.info("📧 Usando Resend API (no SMTP)");

            Resend resend = new Resend(resendApiKey);
            
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject(subject)
                    .html(htmlBody)
                    .build();
            
            log.info("📤 Enviando email HTML via Resend API...");
            CreateEmailResponse response = resend.emails().send(params);
            
            log.info("✅ ¡Email HTML enviado exitosamente a: {}!", to);
            log.info("✅ Resend Email ID: {}", response.getId());

        } catch (ResendException e) {
            log.error("❌ ERROR de Resend enviando email HTML a {}", to);
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
