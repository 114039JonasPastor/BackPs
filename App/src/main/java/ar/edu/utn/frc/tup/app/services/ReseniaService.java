package ar.edu.utn.frc.tup.app.services;

import ar.edu.utn.frc.tup.app.dtos.request.resenia.ReseniaRequest;
import ar.edu.utn.frc.tup.app.dtos.response.resenia.PuntuacionProfesional;
import ar.edu.utn.frc.tup.app.dtos.response.resenia.ReseniaResponse;

public interface ReseniaService {
    ReseniaResponse puntuarResenia(ReseniaRequest reseniaRequest);
    PuntuacionProfesional getPromedioProfesional(Integer idProfesional);
}
