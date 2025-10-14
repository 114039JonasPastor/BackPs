package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.dtos.DomicilioDto;
import ar.edu.utn.frc.tup.app.services.DomicilioService;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/domicilio")
@RequiredArgsConstructor
@Tags(value = {@io.swagger.v3.oas.annotations.tags.Tag(name = "Direcciones")})
public class DireccionController {
    private final DomicilioService domicilioService;

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<DomicilioDto> getDireccion(@PathVariable int idUsuario) {
        return ResponseEntity.ok(domicilioService.getDomicilioUsuario(idUsuario));
    }
}
