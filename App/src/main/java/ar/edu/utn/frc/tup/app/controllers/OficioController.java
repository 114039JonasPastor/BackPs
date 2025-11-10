package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.dtos.common.ErrorApi;
import ar.edu.utn.frc.tup.app.entities.Oficio;
import ar.edu.utn.frc.tup.app.services.OficioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/oficios")
@RequiredArgsConstructor
public class OficioController {

    private final OficioService oficioService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllOficios() {
        List<Oficio> oficios = oficioService.getAllOficios();
        if (oficios.isEmpty()) {
            ErrorApi error = ErrorApi.builder()
                    .timestamp(java.time.Instant.now().toString())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error("Not Found")
                    .message("Oficios no encontrados")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        return ResponseEntity.ok(oficios);
    }
}
