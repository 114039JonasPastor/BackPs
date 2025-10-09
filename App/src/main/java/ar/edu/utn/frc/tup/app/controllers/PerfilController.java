package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.dtos.request.perfil.ModificarCliente;
import ar.edu.utn.frc.tup.app.dtos.response.PerfilCliente;
import ar.edu.utn.frc.tup.app.services.PerfilService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfilService;

    @GetMapping("/cliente/{idUsuario}")
    public ResponseEntity<PerfilCliente> getPerfilCliente(@PathVariable Integer idUsuario){
        return ResponseEntity.ok(perfilService.getPerfilCliente(idUsuario));
    }

    @PutMapping("/cliente")
    public ResponseEntity<PerfilCliente> updatePerfilCliente(@RequestBody ModificarCliente cliente) {
        return ResponseEntity.ok(perfilService.updatePerfilCliente(cliente));
    }
}
