package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.dtos.common.ErrorApi;
import ar.edu.utn.frc.tup.app.dtos.request.solicitud.SolicitudRequest;
import ar.edu.utn.frc.tup.app.dtos.response.solicitud.SolicitudResponse;
import ar.edu.utn.frc.tup.app.dtos.response.solicitud.SolicitudUsuarioResponse;
import ar.edu.utn.frc.tup.app.services.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        List<SolicitudResponse> solicitudes = solicitudService.getSolicitudes(idProfesional, estado);
        if (solicitudes.isEmpty()) {
            ErrorApi error = ErrorApi.builder()
                    .timestamp(java.time.Instant.now().toString())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error("Not Found")
                    .message("Solicitudes no encontradas")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> getSolicitudByIdUsuario(@PathVariable Integer idUsuario) {
        List<SolicitudUsuarioResponse> solicitudes = solicitudService.getSolicitudByIdUsuario(idUsuario);
        if (solicitudes.isEmpty()) {
            ErrorApi error = ErrorApi.builder()
                    .timestamp(java.time.Instant.now().toString())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error("Not Found")
                    .message("Solicitudes no encontradas")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        return ResponseEntity.ok(solicitudes);
    }
}
