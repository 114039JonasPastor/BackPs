package ar.edu.utn.frc.tup.app.services;

import ar.edu.utn.frc.tup.app.dtos.request.trabajo.FinalizarTrabajoRequest;
import ar.edu.utn.frc.tup.app.dtos.response.trabajo.TrabajoResponse;
import ar.edu.utn.frc.tup.app.entities.Factura;
import ar.edu.utn.frc.tup.app.entities.Trabajo;

import java.util.List;

public interface TrabajoService {
    Trabajo crearTrabajo(Integer idSolicitud);
    TrabajoResponse iniciarTrabajo(Integer idTrabajo);
    TrabajoResponse pausarTrabajo(Integer idTrabajo);
    TrabajoResponse reanudarTrabajo(Integer idTrabajo);
    TrabajoResponse finalizarTrabajo(Integer idTrabajo, FinalizarTrabajoRequest request);
    TrabajoResponse cancelarTrabajo(Integer idTrabajo, String motivoCancelacion);
    TrabajoResponse obtenerTrabajoPorId(Integer idTrabajo);
    TrabajoResponse obtenerTrabajoPorSolicitud(Integer idSolicitud);
    List<TrabajoResponse> obtenerTrabajosPorProfesional(Integer idProfesional, String estado);
    List<TrabajoResponse> obtenerTrabajosPorUsuario(Integer idUsuario, String estado);
    List<TrabajoResponse> obtenerTrabajosSinFactura();
}
