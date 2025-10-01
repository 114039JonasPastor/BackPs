package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.entities.Barrio;
import ar.edu.utn.frc.tup.app.entities.Ciudade;
import ar.edu.utn.frc.tup.app.entities.Departamento;
import ar.edu.utn.frc.tup.app.repositories.BarrioRepository;
import ar.edu.utn.frc.tup.app.repositories.CiudadRepository;
import ar.edu.utn.frc.tup.app.repositories.DepartamentoRepository;
import ar.edu.utn.frc.tup.app.services.DepartamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DepartamentoServiceImpl implements DepartamentoService {

    @Autowired
    DepartamentoRepository departamentoRepository;

    @Autowired
    CiudadRepository ciudadRepository;

    @Autowired
    BarrioRepository barrioRepository;

    @Override
    public List<Departamento> getAllDepartamentos() {
        return departamentoRepository.findAll();
    }

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
}
