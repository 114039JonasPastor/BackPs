package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.dtos.request.resenia.ReseniaRequest;
import ar.edu.utn.frc.tup.app.dtos.response.resenia.PuntuacionProfesional;
import ar.edu.utn.frc.tup.app.dtos.response.resenia.ReseniaResponse;
import ar.edu.utn.frc.tup.app.dtos.response.resenia.TopProfesionales;
import ar.edu.utn.frc.tup.app.entities.Oficio;
import ar.edu.utn.frc.tup.app.entities.Profesionale;
import ar.edu.utn.frc.tup.app.entities.Resenia;
import ar.edu.utn.frc.tup.app.entities.Usuario;
import ar.edu.utn.frc.tup.app.repositories.OficioRepository;
import ar.edu.utn.frc.tup.app.repositories.ProfesionalRepository;
import ar.edu.utn.frc.tup.app.repositories.ReseniaRepository;
import ar.edu.utn.frc.tup.app.repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.app.services.ReseniaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReseniaServiceImpl implements ReseniaService {

    private final ReseniaRepository reseniaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProfesionalRepository profesionalRepository;
    private final OficioRepository oficioRepository;

    @Override
    public ReseniaResponse puntuarResenia(ReseniaRequest reseniaRequest) {

        Usuario usuario = usuarioRepository.findById(reseniaRequest.getIdUsuario()).orElse(null);
        if(usuario == null){
            throw new RuntimeException("Usuario no encontrado");
        }

        Profesionale profesional = profesionalRepository.findById(reseniaRequest.getIdProfesional()).orElse(null);
        if (profesional == null) {
            throw new RuntimeException("Profesional no encontrado");
        }

        Resenia resenia = new Resenia();
        resenia.setIdusuario(usuario);
        resenia.setIdprofesional(profesional);
        if(reseniaRequest.getPuntuacion() < 1 || reseniaRequest.getPuntuacion() > 5){
            throw new RuntimeException("La puntuacion debe estar entre 1 y 5");
        }
        resenia.setPuntuacion(reseniaRequest.getPuntuacion());
        resenia.setComentario(reseniaRequest.getComentario());
        resenia.setFecha(Instant.now());

        reseniaRepository.save(resenia);

        ReseniaResponse response = ReseniaResponse.builder().
                nombreUsuario(resenia.getIdusuario().getIdauth().getName() + " " + resenia.getIdusuario().getIdauth().getLastname())
                .nombreProfesional(resenia.getIdprofesional().getIdusuario().getIdauth().getName() + " "
                        + resenia.getIdprofesional().getIdusuario().getIdauth().getLastname())
                .fecha(resenia.getFecha())
                .puntuacion(resenia.getPuntuacion())
                .comentario(resenia.getComentario())
                .build();

        return response;
    }

    @Override
    public PuntuacionProfesional getPromedioProfesional(Integer idProfesional) {
        Profesionale profesional = profesionalRepository.findById(idProfesional)
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado"));

        List<Resenia> resenias = reseniaRepository.findByIdprofesional_Id(idProfesional);
        if (resenias.isEmpty()) {
            throw new RuntimeException("El profesional no tiene reseñas");
        }

        Double total = 0.0;
        for (Resenia r : resenias) {
            total += r.getPuntuacion();
        }

        Double promedio = total / resenias.size();

        return PuntuacionProfesional.builder()
                .nombreProfesional(profesional.getIdusuario().getIdauth().getName() + " " +
                        profesional.getIdusuario().getIdauth().getLastname())
                .puntuacion(promedio)
                .build();
    }

    @Override
    public List<TopProfesionales> getPosicionamientoSegunPuntuacion() {
        List<Oficio> oficios = oficioRepository.findAllWithProfesionales();

        List<TopProfesionales> topProfesionales = new ArrayList<>();

        for (Oficio oficio : oficios) {
            List<Profesionale> profesionalesDelOficio =
                    profesionalRepository.findByIdOficio(oficio.getId());

            Profesionale mejorProfesional = null;
            Double mejorPromedio = 0.0;

            for (Profesionale profesional : profesionalesDelOficio) {
                Long cantidadResenias = reseniaRepository
                        .countReseniasByProfesional(profesional.getId());

                if (cantidadResenias > 0) {
                    Double promedio = reseniaRepository
                            .getPromedioPuntuacionByProfesional(profesional.getId());

                    if (promedio != null && promedio > mejorPromedio) {
                        mejorPromedio = promedio;
                        mejorProfesional = profesional;
                    }
                }
            }

            if (mejorProfesional != null) {
                TopProfesionales top = TopProfesionales.builder()
                        .nombreProfesional(
                                mejorProfesional.getIdusuario().getIdauth().getName() + " " +
                                        mejorProfesional.getIdusuario().getIdauth().getLastname()
                        )
                        .profesion(oficio.getOficio())
                        .puntuacion(Math.round(mejorPromedio * 100.0) / 100.0)
                        .build();

                topProfesionales.add(top);
            }
        }
        topProfesionales.sort((a, b) -> b.getPuntuacion().compareTo(a.getPuntuacion()));

        return topProfesionales;
    }
}
