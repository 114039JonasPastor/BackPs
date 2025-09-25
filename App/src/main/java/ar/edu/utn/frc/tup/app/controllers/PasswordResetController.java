package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.dtos.request.ForgotPasswordRequest;
import ar.edu.utn.frc.tup.app.dtos.request.ResetPasswordRequest;
import ar.edu.utn.frc.tup.app.services.PasswordResetService;
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

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> solicitarRecuperacion(@RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.solicitarRecuperacion(request.getEmail());
            return ResponseEntity.ok(Map.of("mensaje", "Código enviado al email"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al enviar código"));
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
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Código inválido o expirado"));
        }
    }
}
