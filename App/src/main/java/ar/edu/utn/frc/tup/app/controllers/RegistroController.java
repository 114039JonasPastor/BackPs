package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.auth.AuthResponse;
import ar.edu.utn.frc.tup.app.dtos.request.registro.ProfesionalRequest;
import ar.edu.utn.frc.tup.app.dtos.request.registro.UsuarioRequest;
import ar.edu.utn.frc.tup.app.entities.Profesionale;
import ar.edu.utn.frc.tup.app.services.ConfirmationTokenService;
import ar.edu.utn.frc.tup.app.services.RegistroService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/registro")
@Tag(name="Registro")
@RequiredArgsConstructor
public class RegistroController {

    private final RegistroService registroService;
    private final ConfirmationTokenService confirmationTokenService;

    @PostMapping("/usuario")
    public ResponseEntity<AuthResponse> registrarUsuario(@RequestBody UsuarioRequest usuario) {
        return ResponseEntity.ok(registroService.registrarUsuario(usuario));
    }

    //Fixme Posible error en el que el usuario se puede registrar como profesional muchas veces
    @PostMapping("/profesional")
    public ResponseEntity<Profesionale> registrarProfesional(@RequestBody ProfesionalRequest profesionalRequest){
        return ResponseEntity.ok(registroService.registrarProfesional(profesionalRequest));
    }

    @GetMapping("/confirm")
    public ResponseEntity<String> confirmarCuenta(@RequestParam("token") String token) {
        try {
            // Llamar al servicio para confirmar el token
            confirmationTokenService.confirmToken(token);
            return ResponseEntity.ok("Cuenta confirmada exitosamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Token inválido o expirado: " + e.getMessage());
        }
    }
}
