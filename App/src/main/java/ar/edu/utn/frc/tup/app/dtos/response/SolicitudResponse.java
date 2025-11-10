package ar.edu.utn.frc.tup.app.dtos.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class SolicitudResponse {
    private String nombreUsuario;
    private String nombreProfesional;
    private Instant fechasolicitud;
    private Instant fechaservicio;
    private String observacion;
}
