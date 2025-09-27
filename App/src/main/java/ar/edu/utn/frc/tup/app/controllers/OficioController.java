package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.entities.Oficio;
import ar.edu.utn.frc.tup.app.services.OficioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/oficios")
public class OficioController {
    @Autowired
    OficioService oficioService;

    @GetMapping("/all")
    public ResponseEntity<List<Oficio>> getAllOficios() {
        return ResponseEntity.ok(oficioService.getAllOficios());
    }
}
