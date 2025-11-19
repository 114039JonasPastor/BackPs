package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.dtos.request.solicitud.ReprogramarRequest;
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

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudeRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProfesionalRepository profesionalRepository;

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
                .idSolicitud(nueva.getId())
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
            throw new RuntimeException("Solicitudes no encontradas");
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

        LocalTime horaInicioLaboral = LocalTime.of(8, 0);
        LocalTime horaFinLaboral = LocalTime.of(18, 0);

        for (LocalDate fecha = fechaInicio; fecha.isBefore(fechaFin); fecha = fecha.plusDays(1)) {

            java.time.DayOfWeek diaSemana = fecha.getDayOfWeek();
            if (diaSemana == java.time.DayOfWeek.SATURDAY ||
                    diaSemana == java.time.DayOfWeek.SUNDAY) {
                continue;
            }

            List<Solicitude> turnosOcupados = solicitudRepository
                    .findSolicitudesAceptadasByProfesionalAndFecha(idProfesional, fecha);

            Set<LocalTime> horasOcupadas = new HashSet<>();
            for (Solicitude turno : turnosOcupados) {
                LocalDateTime fechaHoraTurno = LocalDateTime.ofInstant(
                        turno.getFechaservicio(),
                        ZoneId.systemDefault()
                );
                LocalTime horaInicio = fechaHoraTurno.toLocalTime();

                Integer duracion = turno.getDuracionEstimada() != null ?
                        turno.getDuracionEstimada() : duracionEstimada;
                LocalTime horaActualTurno = horaInicio;
                LocalTime horaFinTurno = horaInicio.plusMinutes(duracion);

                while (horaActualTurno.isBefore(horaFinTurno)) {
                    horasOcupadas.add(horaActualTurno);
                    horaActualTurno = horaActualTurno.plusMinutes(duracionEstimada);
                }
            }

            LocalTime horaActual = horaInicioLaboral;
            LocalDate finalFecha = fecha;

            while (horaActual.plusMinutes(duracionEstimada).isBefore(horaFinLaboral) ||
                    horaActual.plusMinutes(duracionEstimada).equals(horaFinLaboral)) {

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

    @Override
    public String reprogramarFecha(Integer idSolicitud, ReprogramarRequest request) {
        // 1. Verificar que la solicitud existe
        Solicitude solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // 2. Verificar que la solicitud esté ACEPTADA
        if (!"ACEPTADA".equals(solicitud.getEstado())) {
            throw new RuntimeException("Solo se pueden reprogramar solicitudes aceptadas");
        }

        // 3. Obtener datos necesarios
        Integer idProfesional = solicitud.getIdprofesional().getId();
        LocalDate nuevaFecha = request.getNuevaFecha();
        LocalTime nuevaHora = request.getNuevaHora();
        Integer duracion = request.getDuracion() != null ?
                request.getDuracion() :
                (solicitud.getDuracionEstimada() != null ? solicitud.getDuracionEstimada() : 60);

        // 4. Validar que la nueva fecha sea futura
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime nuevaFechaHora = LocalDateTime.of(nuevaFecha, nuevaHora);

        if (nuevaFechaHora.isBefore(ahora)) {
            throw new RuntimeException("La fecha de reprogramación debe ser futura");
        }

        // 5. Validar que no sea fin de semana
        DayOfWeek diaSemana = nuevaFecha.getDayOfWeek();
        if (diaSemana == DayOfWeek.SATURDAY || diaSemana == DayOfWeek.SUNDAY) {
            throw new RuntimeException("No se pueden programar servicios en fines de semana");
        }

        // 6. Validar horario laboral (8:00 - 18:00)
        LocalTime horaInicioLaboral = LocalTime.of(8, 0);
        LocalTime horaFinLaboral = LocalTime.of(18, 0);
        LocalTime horaFinTurno = nuevaHora.plusMinutes(duracion);

        if (nuevaHora.isBefore(horaInicioLaboral) || horaFinTurno.isAfter(horaFinLaboral)) {
            throw new RuntimeException("El horario debe estar entre las 08:00 y 18:00");
        }

        // 7. Verificar disponibilidad del profesional
        List<Solicitude> turnosOcupados = solicitudRepository
                .findSolicitudesAceptadasByProfesionalAndFecha(idProfesional, nuevaFecha);

        // Excluir la solicitud actual de la verificación
        turnosOcupados = turnosOcupados.stream()
                .filter(s -> !s.getId().equals(idSolicitud))
                .collect(Collectors.toList());

        LocalTime horaFinNuevoTurno = nuevaHora.plusMinutes(duracion);

        for (Solicitude turnoExistente : turnosOcupados) {
            LocalDateTime fechaHoraTurno = LocalDateTime.ofInstant(
                    turnoExistente.getFechaservicio(),
                    ZoneId.systemDefault()
            );
            LocalTime horaInicioExistente = fechaHoraTurno.toLocalTime();
            Integer duracionExistente = turnoExistente.getDuracionEstimada() != null ?
                    turnoExistente.getDuracionEstimada() : duracion;
            LocalTime horaFinExistente = horaInicioExistente.plusMinutes(duracionExistente);

            // Verificar solapamiento
            boolean seSolapan = (nuevaHora.isBefore(horaFinExistente) &&
                    horaFinNuevoTurno.isAfter(horaInicioExistente));

            if (seSolapan) {
                throw new RuntimeException(
                        "El horario seleccionado se solapa con otro turno. " +
                                "Turno ocupado de " + horaInicioExistente + " a " + horaFinExistente
                );
            }
        }

        // 8. Actualizar la solicitud
        LocalDateTime fechaServicio = LocalDateTime.of(nuevaFecha, nuevaHora);
        Instant fechaServicioInstant = fechaServicio
                .atZone(ZoneId.systemDefault())
                .toInstant();

        solicitud.setFechaservicio(fechaServicioInstant);
        solicitud.setDuracionEstimada(duracion);

        solicitudRepository.save(solicitud);

        // 9. Formatear fecha para respuesta
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");
        String fechaFormateada = nuevaFechaHora.format(formatter);

        return "Solicitud reprogramada para el día " + fechaFormateada;
    }

    // Método auxiliar para obtener turnos ocupados (reutilizable)
    private boolean verificarDisponibilidad(
            Integer idProfesional,
            LocalDate fecha,
            LocalTime hora,
            Integer duracion,
            Integer idSolicitudExcluir) {

        List<Solicitude> turnosOcupados = solicitudRepository
                .findSolicitudesAceptadasByProfesionalAndFecha(idProfesional, fecha);

        // Excluir solicitud actual si se proporciona
        if (idSolicitudExcluir != null) {
            turnosOcupados = turnosOcupados.stream()
                    .filter(s -> !s.getId().equals(idSolicitudExcluir))
                    .collect(Collectors.toList());
        }

        LocalTime horaFinNuevo = hora.plusMinutes(duracion);

        for (Solicitude turno : turnosOcupados) {
            LocalDateTime fechaHoraTurno = LocalDateTime.ofInstant(
                    turno.getFechaservicio(),
                    ZoneId.systemDefault()
            );
            LocalTime horaInicio = fechaHoraTurno.toLocalTime();
            Integer duracionTurno = turno.getDuracionEstimada() != null ?
                    turno.getDuracionEstimada() : duracion;
            LocalTime horaFin = horaInicio.plusMinutes(duracionTurno);

            if (hora.isBefore(horaFin) && horaFinNuevo.isAfter(horaInicio)) {
                return false; // Hay solapamiento
            }
        }
        return true; // Está disponible
    }
}
