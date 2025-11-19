package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.dtos.common.ErrorApi;
import ar.edu.utn.frc.tup.app.dtos.request.perfil.ModificarCliente;
import ar.edu.utn.frc.tup.app.dtos.request.perfil.ModificarProfesional;
import ar.edu.utn.frc.tup.app.dtos.response.PerfilCliente;
import ar.edu.utn.frc.tup.app.dtos.response.PerfilProfesional;
import ar.edu.utn.frc.tup.app.entities.Departamento;
import ar.edu.utn.frc.tup.app.services.ConfirmationTokenService;
import ar.edu.utn.frc.tup.app.services.PerfilService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfilService;

    private final ConfirmationTokenService confirmationTokenService;

    @GetMapping("/confirm")
    public ResponseEntity<String> confirmToken(@RequestParam String token) {
        confirmationTokenService.confirmToken(token);
        return ResponseEntity.ok("Cuenta confirmada");
    }

    @GetMapping("/cliente/{idUsuario}")
    public ResponseEntity<?> getPerfilCliente(@PathVariable Integer idUsuario) {
        PerfilCliente perfil = perfilService.getPerfilCliente(idUsuario);
        if (perfil == null) {
            ErrorApi error = ErrorApi.builder()
                    .timestamp(java.time.Instant.now().toString())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error("Not Found")
                    .message("Perfil no encontrado")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        return ResponseEntity.ok(perfil);
    }

    @PutMapping("/cliente")
    public ResponseEntity<PerfilCliente> updatePerfilCliente(@RequestBody ModificarCliente cliente) {
        return ResponseEntity.ok(perfilService.updatePerfilCliente(cliente));
    }

    @GetMapping("/profesional/{idProfesional}")
    public ResponseEntity<?> getPerfilProfesional(@PathVariable Integer idProfesional) {
        PerfilProfesional perfil = perfilService.getPerfilProfesional(idProfesional);
        if (perfil == null) {
            ErrorApi error = ErrorApi.builder()
                    .timestamp(java.time.Instant.now().toString())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error("Not Found")
                    .message("Perfil no encontrado")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        return ResponseEntity.ok(perfil);
    }

    @GetMapping("/profesional/oficio/{oficio}")
    public ResponseEntity<?> getProfesionalesByOficio(@PathVariable String oficio) {
        var profesionales = perfilService.getProfesionalesByOficio(oficio);
        if (profesionales.isEmpty()) {
            ErrorApi error = ErrorApi.builder()
                    .timestamp(java.time.Instant.now().toString())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error("Not Found")
                    .message("No se encontraron profesionales para el oficio especificado")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        return ResponseEntity.ok(profesionales);
    }

    @PutMapping("/profesional")
    public ResponseEntity<PerfilProfesional> updatePerfilProfesional(@RequestBody ModificarProfesional profesional) {
        return ResponseEntity.ok(perfilService.updatePerfilProfesional(profesional));
    }

    @PutMapping("/avatar/{idAuth}")
    public ResponseEntity<String> updateAvatar(@PathVariable Integer idAuth, @RequestBody String avatarUrl) {
        perfilService.updateAvatar(idAuth, avatarUrl);
        return ResponseEntity.ok("Avatar updated successfully");
    }

    @GetMapping("/avatar/{idAuth}")
    public ResponseEntity<?> getAvatar(@PathVariable Integer idAuth) {
        String avatar = perfilService.getAvatar(idAuth);
        if (avatar == null) {
            ErrorApi error = ErrorApi.builder()
                    .timestamp(java.time.Instant.now().toString())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error("Not Found")
                    .message("Avatar no encontrado")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        return ResponseEntity.ok(avatar);
    }
}
