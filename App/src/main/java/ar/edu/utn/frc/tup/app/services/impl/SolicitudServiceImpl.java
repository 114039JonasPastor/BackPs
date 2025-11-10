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

        Usuario usuario = usuarioRepository.findById(solicitud.getIdUsuario()).orElse(null);
        Profesionale profesional = profesionalRepository.findById(solicitud.getIdProfesional()).orElse(null);

        Solicitude nueva = Solicitude.builder()
                .idusuario(usuario)
                .idprofesional(profesional)
                .idoficio(profesional.getIdoficio())
                .fechasolicitud(solicitud.getFechasolicitud())
                .fechaservicio(solicitud.getFechaservicio())
                .estado("PENDIENTE")
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
    public SolicitudResponse getSolicitud(Integer idProfesional, String estado) {

        Profesionale profesionale = profesionalRepository.findById(idProfesional).orElse(null);

        Solicitude solicitud = solicitudRepository.findByIdprofesionalAndEstado(profesionale, estado).orElse(null);
        if (solicitud != null) {
            SolicitudResponse response = SolicitudResponse.builder()
                    .nombreUsuario(solicitud.getIdusuario().getIdauth().getName() + " "
                            + solicitud.getIdusuario().getIdauth().getLastname())
                    .nombreProfesional(solicitud.getIdprofesional().getIdusuario().getIdauth().getName()
                            + " " + solicitud.getIdprofesional().getIdusuario().getIdauth().getLastname())
                    .fechasolicitud(solicitud.getFechasolicitud())
                    .fechaservicio(solicitud.getFechaservicio())
                    .observacion(solicitud.getObservacion())
                    .build();
            return response;
        } else {
            throw new RuntimeException("Solicitud no encontrada");
        }
    }
}
