package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.auth.AuthResponse;
import ar.edu.utn.frc.tup.app.auth.RegisterRequest;
import ar.edu.utn.frc.tup.app.dtos.request.registro.UsuarioRequest;
import ar.edu.utn.frc.tup.app.services.RegistroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/registro")
public class RegistroController {

    @Autowired
    RegistroService registroService;

    @PostMapping("/usuario")
    public ResponseEntity<AuthResponse> registrarUsuario(@RequestBody UsuarioRequest usuario) {
        return ResponseEntity.ok(registroService.registrarUsuario(usuario));
    }
}
