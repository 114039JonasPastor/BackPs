package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.dtos.response.PerfilCliente;
import ar.edu.utn.frc.tup.app.services.PerfilService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/perfil")
public class PerfilController {

    @Autowired
    PerfilService perfilService;

    @GetMapping("/cliente/{idUsuario}")
    public ResponseEntity<PerfilCliente> getPerfilCliente(@PathVariable Integer idUsuario){
        return ResponseEntity.ok(perfilService.getPerfilCliente(idUsuario));
    }
}
