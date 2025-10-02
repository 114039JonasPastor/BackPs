package ar.edu.utn.frc.tup.app.services;

import ar.edu.utn.frc.tup.app.entities.TiposDocumento;
import ar.edu.utn.frc.tup.app.entities.Usuario;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UsuarioService {
    List<TiposDocumento> GetTiposDocumento();
    //Usuario GetUsuario();
}
