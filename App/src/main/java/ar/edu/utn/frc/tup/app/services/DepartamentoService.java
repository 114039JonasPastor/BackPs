package ar.edu.utn.frc.tup.app.services;

import ar.edu.utn.frc.tup.app.entities.Departamento;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DepartamentoService {
    List<Departamento> getAllDepartamentos();
}
