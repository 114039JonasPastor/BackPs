package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.dtos.common.ErrorApi;
import ar.edu.utn.frc.tup.app.dtos.request.perfil.ModificarCliente;
import ar.edu.utn.frc.tup.app.dtos.request.perfil.ModificarProfesional;
import ar.edu.utn.frc.tup.app.dtos.response.UsuariosRegistradosDto;
import ar.edu.utn.frc.tup.app.dtos.response.perfil.PerfilCliente;
import ar.edu.utn.frc.tup.app.dtos.response.perfil.PerfilProfesional;
import ar.edu.utn.frc.tup.app.services.ConfirmationTokenService;
import ar.edu.utn.frc.tup.app.services.PerfilService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
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

    @PutMapping("/profesional")
    public ResponseEntity<PerfilProfesional> updatePerfilProfesional(@RequestBody ModificarProfesional profesional) {
        return ResponseEntity.ok(perfilService.updatePerfilProfesional(profesional));
    }

    @PutMapping("/avatar/{idAuth}")
    public ResponseEntity<String> updateAvatar(@PathVariable Integer idAuth, @RequestBody String avatarUrl) {
        perfilService.updateAvatar(idAuth, avatarUrl);
        return ResponseEntity.ok("Avatar updated successfully");
    }

    @PutMapping("/strike/{idUsuario}")
    public ResponseEntity<String> agregarStrike(@PathVariable Integer idUsuario, @RequestBody String motivo) {
        perfilService.agregarStrike(idUsuario, motivo);
        return ResponseEntity.ok("Strike agregado correctamente");
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

    @GetMapping("/profesionales/oficio")
    public ResponseEntity<?> getProfesionalesByOficio(@RequestParam String oficio) {
        try {
            List<PerfilProfesional> profesionales = perfilService.getProfesionalesByOficio(oficio);

            if (profesionales.isEmpty()) {
                ErrorApi error = ErrorApi.builder()
                        .timestamp(Instant.now().toString())
                        .status(HttpStatus.NOT_FOUND.value())
                        .error("Not Found")
                        .message("No se encontraron profesionales para el oficio: " + oficio)
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            return ResponseEntity.ok(profesionales);
        } catch (RuntimeException e) {
            ErrorApi error = ErrorApi.builder()
                    .timestamp(Instant.now().toString())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .error("Internal Server Error")
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/metrica/usuarios")
    public ResponseEntity<List<?>> getUsuariosMetrica(@RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(perfilService.getUsuariosMetrica(limit));
    }

    @GetMapping("/metrica/profesionales")
    public ResponseEntity<List<?>> getProfesionalesMetrica(@RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(perfilService.getProfesionalesMetrica(limit));
    }

    @GetMapping("/metrica/usuarios-registrados")
    public ResponseEntity<UsuariosRegistradosDto> getUsuariosRegistrados() {
        return ResponseEntity.ok(perfilService.getUsuariosRegistrados());
    }
}