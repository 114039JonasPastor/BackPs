package ar.edu.utn.frc.tup.app.services;

import ar.edu.utn.frc.tup.app.dtos.request.solicitud.SolicitudRequest;
import ar.edu.utn.frc.tup.app.dtos.response.SolicitudResponse;
import ar.edu.utn.frc.tup.app.dtos.response.SolicitudUsuarioResponse;

import java.util.List;

public interface SolicitudService {
    SolicitudResponse enviarSolicitud(SolicitudRequest solicitud);
    String responderSolicitud(Integer idSolicitud, Boolean aceptada);
    List<SolicitudResponse> getSolicitudes(Integer idProfesional, String estado);
    List<SolicitudUsuarioResponse> getSolicitudByIdUsuario(Integer idUsuario);
}
