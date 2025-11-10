package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.dtos.common.ErrorApi;
import ar.edu.utn.frc.tup.app.dtos.request.solicitud.SolicitudRequest;
import ar.edu.utn.frc.tup.app.dtos.response.SolicitudResponse;
import ar.edu.utn.frc.tup.app.services.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

    @PostMapping("/enviar/")
    public ResponseEntity<?> enviarSolicitud(@RequestBody SolicitudRequest solicitud){
        try{
            return ResponseEntity.status(HttpStatus.CREATED).body(solicitudService.enviarSolicitud(solicitud));
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

    @PutMapping("/responder/")
    public ResponseEntity<?> responderSolicitud(@RequestBody Integer idSolicitud, Boolean aceptada){
        try{
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(solicitudService.responderSolicitud(idSolicitud, aceptada));
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

    @GetMapping("/solicitud/{idProfesional}/{estado}")
    public ResponseEntity<?> getSolicitud(@PathVariable Integer idProfesional, @PathVariable String estado) {
        SolicitudResponse solicitud = solicitudService.getSolicitud(idProfesional, estado);
        if (solicitud == null) {
            ErrorApi error = ErrorApi.builder()
                    .timestamp(java.time.Instant.now().toString())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error("Not Found")
                    .message("Solicitud no encontrada")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        return ResponseEntity.ok(solicitud);
    }

}
