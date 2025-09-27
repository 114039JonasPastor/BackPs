package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.dtos.request.ForgotPasswordRequest;
import ar.edu.utn.frc.tup.app.dtos.request.ResetPasswordRequest;
import ar.edu.utn.frc.tup.app.services.PasswordResetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class PasswordResetController {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetController.class);

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> solicitarRecuperacion(@RequestBody ForgotPasswordRequest request) {
        try {
            logger.info("Recibida solicitud de recuperación para: {}", request.getEmail());

            passwordResetService.solicitarRecuperacion(request.getEmail());

            return ResponseEntity.ok(Map.of("mensaje", "Código enviado al email"));

        } catch (Exception e) {
            logger.error("Error en endpoint forgot-password: ", e);

            // Retornar error específico para debug
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Error al enviar código",
                            "detalle", e.getMessage(),
                            "tipo", e.getClass().getSimpleName()
                    ));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> cambiarPassword(@RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.cambiarPassword(
                    request.getEmail(),
                    request.getCodigo(),
                    request.getNuevaPassword()
            );
            return ResponseEntity.ok(Map.of("mensaje", "Contraseña cambiada exitosamente"));
        } catch (Exception e) {
            logger.error("Error en endpoint reset-password: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error", "Error al cambiar contraseña",
                            "detalle", e.getMessage()
                    ));
        }
    }
}
