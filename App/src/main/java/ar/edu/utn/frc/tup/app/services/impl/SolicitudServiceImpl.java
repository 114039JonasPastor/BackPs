package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.dtos.request.solicitud.SolicitudRequest;
import ar.edu.utn.frc.tup.app.dtos.response.solicitud.SolicitudResponse;
import ar.edu.utn.frc.tup.app.dtos.response.solicitud.SolicitudUsuarioResponse;
import ar.edu.utn.frc.tup.app.dtos.response.solicitud.TurnoDisponibleDTO;
import ar.edu.utn.frc.tup.app.entities.Disponibilidad;
import ar.edu.utn.frc.tup.app.entities.Profesionale;
import ar.edu.utn.frc.tup.app.entities.Solicitude;
import ar.edu.utn.frc.tup.app.entities.Usuario;
import ar.edu.utn.frc.tup.app.repositories.DisponibilidadRepository;
import ar.edu.utn.frc.tup.app.repositories.ProfesionalRepository;
import ar.edu.utn.frc.tup.app.repositories.SolicitudeRepository;
import ar.edu.utn.frc.tup.app.repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.app.services.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudeRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProfesionalRepository profesionalRepository;
    private final DisponibilidadRepository disponibilidadRepository;

    @Override
    public SolicitudResponse enviarSolicitud(SolicitudRequest solicitud) {
        Usuario usuario = usuarioRepository.findById(solicitud.getIdUsuario()).orElse(null);
        Profesionale profesional = profesionalRepository.findById(solicitud.getIdProfesional()).orElse(null);

        Solicitude nueva = Solicitude.builder()
                .idusuario(usuario)
                .idprofesional(profesional)
                .idoficio(profesional.getIdoficio())
                .fechasolicitud(solicitud.getFechasolicitud())
                .fechaservicio(solicitud.getFechaservicio())
                .estado("PENDIENTE")
                .iddireccion(usuario.getIddireccion())
                .observacion(solicitud.getObservacion())
                .build();

        solicitudRepository.save(nueva);

        SolicitudResponse response = SolicitudResponse.builder()
                .nombreUsuario(usuario.getIdauth().getName() + " "
                        + usuario.getIdauth().getLastname())
                .nombreProfesional(profesional.getIdusuario().getIdauth().getName()
                        + " " + profesional.getIdusuario().getIdauth().getLastname())
                .fechasolicitud(nueva.getFechasolicitud())
                .fechaservicio(nueva.getFechaservicio())
                .direccion(usuario.getIddireccion().getCalle() + " " + usuario.getIddireccion().getNumero())
                .observacion(nueva.getObservacion())
                .build();

        return response;
    }

    @Override
    public String responderSolicitud(Integer idSolicitud, Boolean aceptada) {
        Solicitude solicitud = solicitudRepository.findById(idSolicitud).orElse(null);
        if(solicitud != null){
            if (aceptada == true){
                solicitud.setEstado("ACEPTADA");
                solicitudRepository.save(solicitud);
                return "Solicitud aceptada";
            } else {
                solicitud.setEstado("RECHAZADA");
                solicitudRepository.save(solicitud);
                return "Solicitud rechazada";
            }
        } else {
            return "La solicitud no existe";
        }
    }

    @Override
    public List<SolicitudResponse> getSolicitudes(Integer idProfesional, String estado) {
        Profesionale profesionale = profesionalRepository.findById(idProfesional).orElse(null);

        List<Solicitude> solicitudes = solicitudRepository.findByIdprofesionalAndEstado(profesionale, estado);
        List<SolicitudResponse> respuestas = new ArrayList<>();
        if (!solicitudes.isEmpty()) {
            for (Solicitude s : solicitudes){
                SolicitudResponse response = SolicitudResponse.builder()
                        .nombreUsuario(s.getIdusuario().getIdauth().getName() + " "
                                + s.getIdusuario().getIdauth().getLastname())
                        .nombreProfesional(s.getIdprofesional().getIdusuario().getIdauth().getName()
                                + " " + s.getIdprofesional().getIdusuario().getIdauth().getLastname())
                        .fechasolicitud(s.getFechasolicitud())
                        .fechaservicio(s.getFechaservicio())
                        .direccion(s.getIdusuario().getIddireccion().getCalle() + " "
                                + s.getIdusuario().getIddireccion().getNumero())
                        .observacion(s.getObservacion())
                        .build();
                respuestas.add(response);
            }
            return respuestas;
        } else {
            throw new RuntimeException("Solicitud no encontrada");
        }
    }

    @Override
    public List<SolicitudUsuarioResponse> getSolicitudByIdUsuario(Integer idUsuario) {
        List<Solicitude> solicitudes = solicitudRepository.findByIdusuario_Id(idUsuario);

        List<SolicitudUsuarioResponse> respuestas = new ArrayList<>();

        if (!solicitudes.isEmpty()) {
            for (Solicitude s : solicitudes) {
                SolicitudUsuarioResponse response = SolicitudUsuarioResponse.builder()
                        .idSolicitud(s.getId())
                        .idProfesional(s.getIdprofesional().getId())
                        .nombreProfesional(s.getIdprofesional().getIdusuario().getIdauth().getName())
                        .apellidoProfesional(s.getIdprofesional().getIdusuario().getIdauth().getLastname())
                        .fechaSolicitud(s.getFechasolicitud())
                        .estado(s.getEstado())
                        .imagenUrl(s.getIdprofesional().getIdusuario().getAvatar())
                        .build();
                respuestas.add(response);
            }
            return respuestas;
        } else {
            throw new RuntimeException("Solicitudes no encontradas");
        }
    }

    @Override
    public List<TurnoDisponibleDTO> obtenerTurnosDisponiblesSemana(
            Integer idProfesional, LocalDate fechaInicio, Integer duracionEstimada) {

        Profesionale profesional = profesionalRepository.findById(idProfesional)
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado"));

        List<TurnoDisponibleDTO> turnosDisponibles = new ArrayList<>();
        LocalDate fechaFin = fechaInicio.plusDays(7);

        // Horario laboral fijo
        LocalTime horaInicioLaboral = LocalTime.of(8, 0);
        LocalTime horaFinLaboral = LocalTime.of(18, 0);

        // Iterar cada día de la semana
        for (LocalDate fecha = fechaInicio; fecha.isBefore(fechaFin); fecha = fecha.plusDays(1)) {

            // Verificar si es fin de semana
            java.time.DayOfWeek diaSemana = fecha.getDayOfWeek();
            if (diaSemana == java.time.DayOfWeek.SATURDAY ||
                    diaSemana == java.time.DayOfWeek.SUNDAY) {
                continue;
            }

            // Obtener todas las solicitudes ACEPTADAS para este día
            List<Solicitude> turnosOcupados = solicitudRepository
                    .findSolicitudesAceptadasByProfesionalAndFecha(idProfesional, fecha);

            // Crear un Set con las horas ocupadas para búsqueda rápida
            Set<LocalTime> horasOcupadas = new HashSet<>();
            for (Solicitude turno : turnosOcupados) {
                LocalDateTime fechaHoraTurno = LocalDateTime.ofInstant(
                        turno.getFechaservicio(),
                        ZoneId.systemDefault()
                );
                LocalTime horaInicio = fechaHoraTurno.toLocalTime();

                // Agregar la hora de inicio y todas las horas que ocupa el turno
                Integer duracion = turno.getDuracionEstimada() != null ?
                        turno.getDuracionEstimada() : duracionEstimada;
                LocalTime horaActualTurno = horaInicio;
                LocalTime horaFinTurno = horaInicio.plusMinutes(duracion);

                // Marcar todos los slots que este turno ocupa
                while (horaActualTurno.isBefore(horaFinTurno)) {
                    horasOcupadas.add(horaActualTurno);
                    horaActualTurno = horaActualTurno.plusMinutes(duracionEstimada);
                }
            }

            // Generar todos los slots del día
            LocalTime horaActual = horaInicioLaboral;
            LocalDate finalFecha = fecha;

            while (horaActual.plusMinutes(duracionEstimada).isBefore(horaFinLaboral) ||
                    horaActual.plusMinutes(duracionEstimada).equals(horaFinLaboral)) {

                // Solo agregar si la hora NO está ocupada
                if (!horasOcupadas.contains(horaActual)) {
                    turnosDisponibles.add(TurnoDisponibleDTO.builder()
                            .fecha(finalFecha)
                            .horaInicio(horaActual)
                            .horaFin(horaActual.plusMinutes(duracionEstimada))
                            .duracionEstimada(duracionEstimada)
                            .build());
                }

                horaActual = horaActual.plusMinutes(duracionEstimada);
            }
        }

        return turnosDisponibles;
    }

    @Override
    public SolicitudResponse confirmarTurno(Integer idUsuario, Integer idProfesional,
                                            LocalDate fecha, java.time.LocalTime hora,
                                            Integer duracion, String observacion) {

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Profesionale profesional = profesionalRepository.findById(idProfesional)
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado"));

        // Validar que el turno esté disponible
        List<Solicitude> turnosOcupados = solicitudRepository
                .findSolicitudesAceptadasByProfesionalAndFecha(idProfesional, fecha);

        LocalTime horaFinNuevoTurno = hora.plusMinutes(duracion);

        for (Solicitude turnoExistente : turnosOcupados) {
            LocalDateTime fechaHoraTurno = LocalDateTime.ofInstant(
                    turnoExistente.getFechaservicio(),
                    ZoneId.systemDefault()
            );
            LocalTime horaInicioExistente = fechaHoraTurno.toLocalTime();
            Integer duracionExistente = turnoExistente.getDuracionEstimada() != null ?
                    turnoExistente.getDuracionEstimada() : duracion;
            LocalTime horaFinExistente = horaInicioExistente.plusMinutes(duracionExistente);

            // Verificar solapamiento de horarios
            boolean seSolapan = (hora.isBefore(horaFinExistente) && horaFinNuevoTurno.isAfter(horaInicioExistente));

            if (seSolapan) {
                throw new RuntimeException("El turno seleccionado ya no está disponible");
            }
        }

        java.time.LocalDateTime fechaServicio = java.time.LocalDateTime.of(fecha, hora);
        java.time.Instant fechaServicioInstant = fechaServicio
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant();

        Solicitude turno = Solicitude.builder()
                .idusuario(usuario)
                .idprofesional(profesional)
                .idoficio(profesional.getIdoficio())
                .fechasolicitud(java.time.Instant.now())
                .fechaservicio(fechaServicioInstant)
                .estado("ACEPTADA")
                .esTurno(true)
                .duracionEstimada(duracion)
                .iddireccion(usuario.getIddireccion())
                .observacion(observacion)
                .build();

        solicitudRepository.save(turno);

        return SolicitudResponse.builder()
                .nombreUsuario(usuario.getIdauth().getName() + " " + usuario.getIdauth().getLastname())
                .nombreProfesional(profesional.getIdusuario().getIdauth().getName() + " " +
                        profesional.getIdusuario().getIdauth().getLastname())
                .fechasolicitud(turno.getFechasolicitud())
                .fechaservicio(turno.getFechaservicio())
                .direccion(usuario.getIddireccion().getCalle() + " " + usuario.getIddireccion().getNumero())
                .observacion(turno.getObservacion())
                .build();
    }
}
