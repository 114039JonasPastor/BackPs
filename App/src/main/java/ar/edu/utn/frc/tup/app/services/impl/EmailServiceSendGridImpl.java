package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.services.EmailService;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
@Primary  // SendGrid tiene prioridad - funciona en Render sin restricciones
public class EmailServiceSendGridImpl implements EmailService {

    @Value("${sendgrid.api.key}")
    private String sendgridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name:TuOficio}")
    private String fromName;

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
            log.info("📧 Usando SendGrid API desde: {} <{}>", fromName, fromEmail);
            
            Email from = new Email(fromEmail, fromName);
            Email toEmail = new Email(to);
            Content content = new Content("text/plain", body);
            Mail mail = new Mail(from, subject, toEmail, content);
            
            SendGrid sg = new SendGrid(sendgridApiKey);
            Request request = new Request();
            
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            log.info("📤 Enviando email via SendGrid API...");
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("✅ Email enviado exitosamente via SendGrid (Status: {})", response.getStatusCode());
            } else {
                log.error("❌ SendGrid respondió con código: {}", response.getStatusCode());
                log.error("❌ Body: {}", response.getBody());
                throw new RuntimeException("SendGrid error: " + response.getStatusCode());
            }
            
        } catch (IOException e) {
            log.error("❌ Error de SendGrid enviando email a {}", to);
            log.error("❌ Mensaje: {}", e.getMessage());
            throw new RuntimeException("Error enviando email: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendHtml(String to, String subject, String htmlBody) {
        try {
            log.info("📧 Preparando email HTML para: {}", to);
            log.info("📧 Asunto: {}", subject);
            log.info("📧 Usando SendGrid API desde: {} <{}>", fromName, fromEmail);

            Email from = new Email(fromEmail, fromName);
            Email toEmail = new Email(to);
            Content content = new Content("text/html", htmlBody);
            Mail mail = new Mail(from, subject, toEmail, content);
            
            SendGrid sg = new SendGrid(sendgridApiKey);
            Request request = new Request();
            
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            log.info("📤 Enviando email HTML via SendGrid API...");
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("✅ ¡Email HTML enviado exitosamente a: {}!", to);
                log.info("✅ SendGrid Status Code: {}", response.getStatusCode());
            } else {
                log.error("❌ SendGrid respondió con código: {}", response.getStatusCode());
                log.error("❌ Body: {}", response.getBody());
                log.error("❌ Headers: {}", response.getHeaders());
                throw new RuntimeException("SendGrid error: " + response.getStatusCode() + " - " + response.getBody());
            }

        } catch (IOException e) {
            log.error("❌ ERROR de SendGrid enviando email HTML a {}", to);
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
