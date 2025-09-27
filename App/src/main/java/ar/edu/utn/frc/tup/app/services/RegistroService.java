package ar.edu.utn.frc.tup.app.services;

import ar.edu.utn.frc.tup.app.dtos.request.registro.NuevoCliente;
import ar.edu.utn.frc.tup.app.dtos.request.registro.NuevoProfesional;
import ar.edu.utn.frc.tup.app.entities.Cliente;
import ar.edu.utn.frc.tup.app.entities.Profesionale;
import org.springframework.stereotype.Service;

@Service
public interface RegistroService {
    Profesionale registrarProfesional(NuevoProfesional nuevo);
    Cliente registrarCliente(NuevoCliente nuevo);
}
