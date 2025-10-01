package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.entities.Departamento;
import ar.edu.utn.frc.tup.app.repositories.DepartamentoRepository;
import ar.edu.utn.frc.tup.app.services.DepartamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartamentoServiceImpl implements DepartamentoService {

    @Autowired
    DepartamentoRepository departamentoRepository;

    @Override
    public List<Departamento> getAllDepartamentos() {
        return departamentoRepository.findAll();
    }
<<<<<<< Updated upstream
=======

    @Override
    public Optional<Departamento> getDepartamentoById(int id) { return departamentoRepository.findById(id); }

    @Override
    public List<Ciudade> getAllCiudades() {
        return ciudadRepository.findAll();
    }

    @Override
    public Optional<Ciudade> getCiudadById(int id) {
        return ciudadRepository.findById(id);
    }

    @Override
    public List<Barrio> getAllBarrios() {
        return barrioRepository.findAll();
    }

    @Override
    public Optional<Barrio> getBarrioById(int id) {
        return barrioRepository.findById(id);
    }

    @Override
    public List<Barrio> getBarriosByCiudadId(int ciudadId) {
        Ciudade ciudad = ciudadRepository.findById(ciudadId).orElse(null);
        return barrioRepository.findByIdciudad_Ciudad(ciudad.getCiudad());
    }

    @Override
    public List<Ciudade> getCiudadesByDepartamentoId(int departamentoId) {
        Departamento departamento = departamentoRepository.findById(departamentoId).orElse(null);
        return ciudadRepository.findByIddepartamento_Departamento(departamento.getDepartamento());
    }
>>>>>>> Stashed changes
}
