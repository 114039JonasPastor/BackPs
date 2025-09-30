package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.dtos.request.registro.UsuarioRequest;
import ar.edu.utn.frc.tup.app.entities.Direccione;
import ar.edu.utn.frc.tup.app.entities.TiposDocumento;
import ar.edu.utn.frc.tup.app.entities.Usuario;
import ar.edu.utn.frc.tup.app.repositories.AuthRepository;
import ar.edu.utn.frc.tup.app.repositories.DireccionRepository;
import ar.edu.utn.frc.tup.app.repositories.TipoDocumentoRepository;
import ar.edu.utn.frc.tup.app.repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.app.services.RegistroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegistroServiceImpl implements RegistroService {

    @Autowired
    AuthRepository authRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    TipoDocumentoRepository tipoDocumentoRepository;

    @Autowired
    DireccionRepository direccioneRepository;

    @Override
    public Usuario registrarUsuario(UsuarioRequest usuario) {
        TiposDocumento tipo = tipoDocumentoRepository.findById(usuario.getIdTipoDoc()).orElse(null);
        Direccione direccion = direccioneRepository.findById(usuario.getIdDireccion()).orElse(null);

        Usuario nuevo = new Usuario();


        return null;
    }
}
