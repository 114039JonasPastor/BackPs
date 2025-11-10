package ar.edu.utn.frc.tup.app.dtos.request.solicitud;

import lombok.Data;

import java.time.Instant;

@Data
public class SolicitudRequest {
    private Integer idUsuario;
    private Integer idProfesional;
//    private Integer idoficio;
    private Instant fechasolicitud;
    private Instant fechaservicio;
//    private String estado;
    private String observacion;
}
