package ar.edu.utn.frc.tup.app.services;

import ar.edu.utn.frc.tup.app.dtos.response.PerfilCliente;
import org.springframework.stereotype.Service;

@Service
public interface PerfilService {
    PerfilCliente getPerfilCliente(Integer idCliente);
}
