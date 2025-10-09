package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.auth.AuthResponse;
import ar.edu.utn.frc.tup.app.auth.RegisterRequest;
import ar.edu.utn.frc.tup.app.dtos.request.registro.ProfesionalRequest;
import ar.edu.utn.frc.tup.app.dtos.request.registro.UsuarioRequest;
import ar.edu.utn.frc.tup.app.entities.Profesionale;
import ar.edu.utn.frc.tup.app.services.RegistroService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/registro")
@Tag(name="Registro")
@RequiredArgsConstructor
public class RegistroController {

    private final RegistroService registroService;

    @PostMapping("/usuario")
    public ResponseEntity<AuthResponse> registrarUsuario(@RequestBody UsuarioRequest usuario) {
        return ResponseEntity.ok(registroService.registrarUsuario(usuario));
    }

    //Fixme Posible error en el que el usuario se puede registrar como profesional muchas veces
    @PostMapping("/profesional")
    public ResponseEntity<Profesionale> registrarProfesional(@RequestBody ProfesionalRequest profesionalRequest){
        return ResponseEntity.ok(registroService.registrarProfesional(profesionalRequest));
    }
}
