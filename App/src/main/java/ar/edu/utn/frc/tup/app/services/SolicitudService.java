package ar.edu.utn.frc.tup.app.services;

import ar.edu.utn.frc.tup.app.dtos.request.solicitud.SolicitudRequest;
import ar.edu.utn.frc.tup.app.dtos.response.SolicitudResponse;

public interface SolicitudService {
    SolicitudResponse enviarSolicitud(SolicitudRequest solicitud);
    String responderSolicitud(Integer idSolicitud, Boolean aceptada);
    SolicitudResponse getSolicitud(Integer idProfesional, String estado);
}
