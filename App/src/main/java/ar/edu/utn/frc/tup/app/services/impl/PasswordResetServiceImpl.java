package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.entities.PasswordResetToken;
import ar.edu.utn.frc.tup.app.entities.Usuario;
import ar.edu.utn.frc.tup.app.repositories.PasswordResetTokenRepository;
import ar.edu.utn.frc.tup.app.repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.app.services.EmailService;
import ar.edu.utn.frc.tup.app.services.PasswordResetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@Slf4j // Para logs
public class PasswordResetServiceImpl implements PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void solicitarRecuperacion(String email) {
        try {
            log.info("Iniciando recuperación para email: {}", email);

            // Verificar que el usuario existe
            Usuario usuario = usuarioRepository.findByMail(email)
                    .orElseThrow(() -> {
                        log.error("Usuario no encontrado: {}", email);
                        return new RuntimeException("Email no encontrado");
                    });

            log.info("Usuario encontrado: {}", usuario.getId());

            // Generar código de 6 dígitos
            String codigo = String.format("%06d", new Random().nextInt(999999));
            log.info("Código generado para {}: {}", email, codigo);

            // Guardar token
            PasswordResetToken token = new PasswordResetToken();
            token.setToken(codigo);
            token.setEmail(email);
//            token.setUsuario(usuario); // ✅ AGREGAR ESTA LÍNEA
            token.setExpiryDate(LocalDateTime.now().plusMinutes(15));
            token.setUsed(false);

            PasswordResetToken savedToken = tokenRepository.save(token);
            log.info("Token guardado con ID: {}", savedToken.getId());

            // Enviar email
            emailService.enviarCodigoRecuperacion(email, codigo);
            log.info("Email enviado exitosamente a: {}", email);

        } catch (Exception e) {
            log.error("Error en solicitarRecuperacion: ", e);
            throw new RuntimeException("Error procesando solicitud: " + e.getMessage(), e);
        }
    }

    @Override
    public void cambiarPassword(String email, String codigo, String nuevaPassword) {
        try {
            log.info("Intentando cambiar password para: {}", email);

            PasswordResetToken token = tokenRepository
                    .findByEmailAndTokenAndUsedFalseAndExpiryDateAfter(
                            email, codigo, LocalDateTime.now())
                    .orElseThrow(() -> {
                        log.error("Token inválido o expirado para: {}", email);
                        return new RuntimeException("Código inválido o expirado");
                    });

            // Cambiar contraseña
            Usuario usuario = usuarioRepository.findByMail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            usuario.setPassword(passwordEncoder.encode(nuevaPassword));
            usuarioRepository.save(usuario);

            // Marcar token como usado
            token.setUsed(true);
            tokenRepository.save(token);

            log.info("Password cambiado exitosamente para: {}", email);

        } catch (Exception e) {
            log.error("Error cambiando password: ", e);
            throw new RuntimeException("Error cambiando password: " + e.getMessage(), e);
        }
    }
}