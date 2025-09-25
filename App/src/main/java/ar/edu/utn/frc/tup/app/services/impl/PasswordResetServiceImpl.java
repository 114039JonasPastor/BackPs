package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.entities.PasswordResetToken;
import ar.edu.utn.frc.tup.app.entities.Usuario;
import ar.edu.utn.frc.tup.app.repositories.PasswordResetTokenRepository;
import ar.edu.utn.frc.tup.app.repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.app.services.EmailService;
import ar.edu.utn.frc.tup.app.services.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void solicitarRecuperacion(String email) {
        // Verificar que el usuario existe
        Usuario usuario = usuarioRepository.findByMail(email)
                .orElseThrow(() -> new RuntimeException("Email no encontrado"));

        // Generar código de 6 dígitos
        String codigo = String.format("%06d", new Random().nextInt(999999));

        // Guardar token
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(codigo);
        token.setEmail(email);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(15)); // 15 min
        token.setUsed(false);

        tokenRepository.save(token);

        // Enviar email
        emailService.enviarCodigoRecuperacion(email, codigo);
    }

    public void cambiarPassword(String email, String codigo, String nuevaPassword) {
        PasswordResetToken token = tokenRepository
                .findByEmailAndTokenAndUsedFalseAndExpiryDateAfter(
                        email, codigo, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Código inválido o expirado"));

        // Cambiar contraseña
        Usuario usuario = usuarioRepository.findByMail(email).orElseThrow();
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        // Marcar token como usado
        token.setUsed(true);
        tokenRepository.save(token);
    }
}
