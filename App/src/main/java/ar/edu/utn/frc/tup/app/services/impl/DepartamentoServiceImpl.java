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
}
