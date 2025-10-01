package ar.edu.utn.frc.tup.app.services;

import ar.edu.utn.frc.tup.app.entities.Departamento;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DepartamentoService {
    List<Departamento> getAllDepartamentos();
<<<<<<< Updated upstream
=======
    Optional<Departamento> getDepartamentoById(int id);
    List<Ciudade> getAllCiudades();
    Optional<Ciudade> getCiudadById(int id);
    List<Barrio> getAllBarrios();
    Optional<Barrio> getBarrioById(int id);
    List<Barrio> getBarriosByCiudadId(int ciudadId);
    List<Ciudade> getCiudadesByDepartamentoId(int departamentoId);
>>>>>>> Stashed changes
}
