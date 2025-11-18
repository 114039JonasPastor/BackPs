package ar.edu.utn.frc.tup.app.services;

import ar.edu.utn.frc.tup.app.dtos.request.solicitud.SolicitudRequest;
import ar.edu.utn.frc.tup.app.dtos.response.solicitud.SolicitudResponse;
import ar.edu.utn.frc.tup.app.dtos.response.solicitud.SolicitudUsuarioResponse;
import ar.edu.utn.frc.tup.app.dtos.response.solicitud.TurnoDisponibleDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface SolicitudService {
    SolicitudResponse enviarSolicitud(SolicitudRequest solicitud);
    String responderSolicitud(Integer idSolicitud, Boolean aceptada);
    List<SolicitudResponse> getSolicitudes(Integer idProfesional, String estado);
    List<SolicitudUsuarioResponse> getSolicitudByIdUsuario(Integer idUsuario);
    List<TurnoDisponibleDTO> obtenerTurnosDisponiblesSemana(Integer idProfesional, LocalDate fechaInicio, Integer duracionEstimada);
    SolicitudResponse confirmarTurno(Integer idUsuario, Integer idProfesional, LocalDate fecha, LocalTime hora, Integer duracion, String observacion);
}
