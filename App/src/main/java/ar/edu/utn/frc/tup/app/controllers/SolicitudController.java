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

    @PutMapping("/responder/{idSolicitud}")
    public ResponseEntity<?> responderSolicitud(@PathVariable Integer idSolicitud, @RequestParam Boolean aceptada){
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
        try {
            SolicitudResponse solicitud = solicitudService.getSolicitud(idProfesional, estado);
            
            // Si no hay solicitudes, retornar 204 No Content (no es un error, simplemente no hay datos)
            if (solicitud == null) {
                return ResponseEntity.noContent().build();
            }
            
            return ResponseEntity.ok(solicitud);
        } catch (RuntimeException e) {
            // Solo si hay un error real (ej: profesional no existe)
            ErrorApi error = ErrorApi.builder()
                    .timestamp(java.time.Instant.now().toString())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error("Bad Request")
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(error);
        }
    }

}
