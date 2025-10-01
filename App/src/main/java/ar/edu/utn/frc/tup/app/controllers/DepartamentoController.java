package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.entities.Barrio;
import ar.edu.utn.frc.tup.app.entities.Ciudade;
import ar.edu.utn.frc.tup.app.entities.Departamento;
import ar.edu.utn.frc.tup.app.services.DepartamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/domicilios")
public class DepartamentoController {

    @Autowired
    DepartamentoService departamentoService;

    @GetMapping("/departamentos/all")
    public ResponseEntity<List<Departamento>> getAllOficios() {
        return ResponseEntity.ok(departamentoService.getAllDepartamentos());
    }

    @GetMapping("/ciudades/all")
    public ResponseEntity<List<Ciudade>> getAllCiudades() {
        return ResponseEntity.ok(departamentoService.getAllCiudades());
    }

    @GetMapping("/barrios/all")
    public ResponseEntity<List<Barrio>> getAllBarrios() {
        return ResponseEntity.ok(departamentoService.getAllBarrios());
    }

    @GetMapping(value = "/departamento/{id}")
    public ResponseEntity<Departamento> getDepartamentoById(@PathVariable int id) {
        return ResponseEntity.of(departamentoService.getDepartamentoById(id));
    }

    @GetMapping(value = "/ciudad/{id}")
    public ResponseEntity<Ciudade> getCiudadById(@PathVariable int id) {
        return ResponseEntity.of(departamentoService.getCiudadById(id));
    }

    @GetMapping(value = "/barrio/{id}")
    public ResponseEntity<Barrio> getBarrioById(@PathVariable int id) {
        return ResponseEntity.of(departamentoService.getBarrioById(id));
    }
}
