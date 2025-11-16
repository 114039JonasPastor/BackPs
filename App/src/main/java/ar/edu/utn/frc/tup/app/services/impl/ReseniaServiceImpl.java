package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.dtos.request.resenia.ReseniaRequest;
import ar.edu.utn.frc.tup.app.dtos.response.ReseniaResponse;
import ar.edu.utn.frc.tup.app.entities.Profesionale;
import ar.edu.utn.frc.tup.app.entities.Resenia;
import ar.edu.utn.frc.tup.app.entities.Usuario;
import ar.edu.utn.frc.tup.app.repositories.ProfesionalRepository;
import ar.edu.utn.frc.tup.app.repositories.ReseniaRepository;
import ar.edu.utn.frc.tup.app.repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.app.services.ReseniaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ReseniaServiceImpl implements ReseniaService {

    private final ReseniaRepository reseniaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProfesionalRepository profesionalRepository;

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
}
