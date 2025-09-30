package ar.edu.utn.frc.tup.app.services;

import ar.edu.utn.frc.tup.app.dtos.request.registro.UsuarioRequest;
import ar.edu.utn.frc.tup.app.entities.Usuario;
import org.springframework.stereotype.Service;

@Service
public interface RegistroService {
    Usuario registrarUsuario(UsuarioRequest usuario);
}
