package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

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
}
