package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.dtos.request.solicitud.SolicitudRequest;
import ar.edu.utn.frc.tup.app.dtos.response.SolicitudResponse;
import ar.edu.utn.frc.tup.app.entities.Profesionale;
import ar.edu.utn.frc.tup.app.entities.Solicitude;
import ar.edu.utn.frc.tup.app.entities.Usuario;
import ar.edu.utn.frc.tup.app.repositories.ProfesionalRepository;
import ar.edu.utn.frc.tup.app.repositories.SolicitudeRepository;
import ar.edu.utn.frc.tup.app.repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.app.services.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudeRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProfesionalRepository profesionalRepository;

    @Override
    public SolicitudResponse enviarSolicitud(SolicitudRequest solicitud) { //TODO Muy importante, agregar despues a este metodo y a la clase de solicitud la direccion de la solicitud

        Usuario usuario = usuarioRepository.findById(solicitud.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + solicitud.getIdUsuario()));

        Profesionale profesional = profesionalRepository.findById(solicitud.getIdProfesional())
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado con ID: " + solicitud.getIdProfesional()));

        if (usuario.getIddireccion() == null) {
            throw new RuntimeException("El usuario no tiene una dirección registrada");
        }

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

        Solicitude solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("La solicitud no existe con ID: " + idSolicitud));

        if (Boolean.TRUE.equals(aceptada)) {
            solicitud.setEstado("ACEPTADA");
            solicitudRepository.save(solicitud);
            return "Solicitud aceptada";
        } else {
            solicitud.setEstado("RECHAZADA");
            solicitudRepository.save(solicitud);
            return "Solicitud rechazada";
        }
    }

    @Override
    public SolicitudResponse getSolicitud(Integer idProfesional, String estado) {

        Profesionale profesionale = profesionalRepository.findById(idProfesional)
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado con ID: " + idProfesional));

        Solicitude solicitud = solicitudRepository.findByIdprofesionalAndEstado(profesionale, estado)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada para el profesional ID: " + idProfesional + " con estado: " + estado));

        SolicitudResponse response = SolicitudResponse.builder()
                .nombreUsuario(solicitud.getIdusuario().getIdauth().getName() + " "
                        + solicitud.getIdusuario().getIdauth().getLastname())
                .nombreProfesional(solicitud.getIdprofesional().getIdusuario().getIdauth().getName()
                        + " " + solicitud.getIdprofesional().getIdusuario().getIdauth().getLastname())
                .fechasolicitud(solicitud.getFechasolicitud())
                .fechaservicio(solicitud.getFechaservicio())
                .direccion(solicitud.getIdusuario().getIddireccion().getCalle() + " "
                        + solicitud.getIdusuario().getIddireccion().getNumero())
                .observacion(solicitud.getObservacion())
                .build();

        return response;
    }
}
