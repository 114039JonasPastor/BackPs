package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.dtos.request.registro.NuevoCliente;
import ar.edu.utn.frc.tup.app.entities.Cliente;
import ar.edu.utn.frc.tup.app.services.RegistroService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RegistroController {

    @Autowired
    RegistroService registroService;

    @PostMapping("/registro/cliente")
    public ResponseEntity<Cliente> registrarCliente(@RequestBody NuevoCliente nuevoCliente) {
        return ResponseEntity.ok(registroService.registrarCliente(nuevoCliente));
    }
}
