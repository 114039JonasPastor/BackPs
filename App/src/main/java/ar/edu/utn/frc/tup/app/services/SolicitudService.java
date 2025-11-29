package ar.edu.utn.frc.tup.app.services;

import ar.edu.utn.frc.tup.app.dtos.request.solicitud.ReprogramarRequest;
import ar.edu.utn.frc.tup.app.dtos.request.solicitud.SolicitudRequest;
import ar.edu.utn.frc.tup.app.dtos.response.solicitud.SolicitudResponse;
import ar.edu.utn.frc.tup.app.dtos.response.solicitud.SolicitudUsuarioResponse;
import ar.edu.utn.frc.tup.app.dtos.response.solicitud.TurnoDisponibleDTO;
import ar.edu.utn.frc.tup.app.entities.Solicitude;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface SolicitudService {

    SolicitudResponse enviarSolicitud(SolicitudRequest solicitud);

    String responderSolicitud(Integer idSolicitud, Boolean aceptada);

    List<SolicitudResponse> getSolicitudes(Integer idProfesional, String estado);

    List<SolicitudUsuarioResponse> getSolicitudByIdUsuario(Integer idUsuario);

    List<TurnoDisponibleDTO> obtenerTurnosDisponiblesSemana(
            Integer idProfesional, LocalDate fechaInicio, Integer duracionEstimada);

    SolicitudResponse confirmarTurno(
            Integer idUsuario, Integer idProfesional,
            LocalDate fecha, LocalTime hora,
            Integer duracion, String observacion);

    String reprogramarFecha(Integer idSolicitud, ReprogramarRequest request);

    boolean tieneSolicitudPendiente(Integer idUsuario, Integer idProfesional);

    // ✅ Métodos para el mapa
    Map<String, Object> getSolicitudConUbicacion(Integer idSolicitud);

    List<Map<String, Object>> getSolicitudesByProfesionalConUbicacion(Integer idProfesional);

    // ✅ Método para obtener solicitud por ID
    Solicitude getSolicitudById(Integer idSolicitud);
}
