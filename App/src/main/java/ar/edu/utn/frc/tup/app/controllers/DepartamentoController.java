package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.entities.Departamento;
import ar.edu.utn.frc.tup.app.services.DepartamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departamentos")
public class DepartamentoController {

    @Autowired
    DepartamentoService departamentoService;

    @GetMapping("/all")
    public ResponseEntity<List<Departamento>> getAllOficios() {
        return ResponseEntity.ok(departamentoService.getAllDepartamentos());
    }
}
