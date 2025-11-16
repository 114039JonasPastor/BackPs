package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.dtos.DomicilioDto;
import ar.edu.utn.frc.tup.app.dtos.common.ErrorApi;
import ar.edu.utn.frc.tup.app.dtos.request.resenia.ReseniaRequest;
import ar.edu.utn.frc.tup.app.dtos.response.resenia.PuntuacionProfesional;
import ar.edu.utn.frc.tup.app.services.ReseniaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/resenias")
@RequiredArgsConstructor
public class ReseniaController {

    private final ReseniaService reseniaService;

    @PostMapping("/puntuar/")
    public ResponseEntity<?> puntuarResenia(@RequestBody ReseniaRequest reseniaRequest){
        try{
            return ResponseEntity.status(201).body(reseniaService.puntuarResenia(reseniaRequest));
        } catch (RuntimeException e){
            ErrorApi error = ErrorApi.builder()
                    .timestamp(java.time.Instant.now().toString())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error("Bad Request")
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/promedio/{idProfesional}")
    public ResponseEntity<?> getPromedioProfesional(@PathVariable Integer idProfesional){
        PuntuacionProfesional puntuacion = reseniaService.getPromedioProfesional(idProfesional);
        if (puntuacion == null) {
            ErrorApi error = ErrorApi.builder()
                    .timestamp(java.time.Instant.now().toString())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error("Not Found")
                    .message("Este profesional no tiene puntuaciones")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        return ResponseEntity.ok(puntuacion);
    }
}
