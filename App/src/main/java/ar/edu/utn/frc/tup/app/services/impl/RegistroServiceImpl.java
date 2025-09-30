package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.auth.AuthResponse;
import ar.edu.utn.frc.tup.app.auth.services.JwtService;
import ar.edu.utn.frc.tup.app.dtos.request.registro.UsuarioRequest;
import ar.edu.utn.frc.tup.app.entities.Auth;
import ar.edu.utn.frc.tup.app.entities.Direccione;
import ar.edu.utn.frc.tup.app.entities.TiposDocumento;
import ar.edu.utn.frc.tup.app.entities.Usuario;
import ar.edu.utn.frc.tup.app.repositories.AuthRepository;
import ar.edu.utn.frc.tup.app.repositories.DireccionRepository;
import ar.edu.utn.frc.tup.app.repositories.TipoDocumentoRepository;
import ar.edu.utn.frc.tup.app.repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.app.services.RegistroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtService jwtService;

    @Override
    public AuthResponse registrarUsuario(UsuarioRequest usuario) { //TODO revisar
        Auth auth = Auth.builder()
                .password(passwordEncoder.encode(usuario.getPassword()))
                .mail(usuario.getEmail())
                .name(usuario.getName())
                .lastname(usuario.getLastName())
                .active(true) //Todo revisar
                .build();
        authRepository.save(auth);

        TiposDocumento tipo = tipoDocumentoRepository.findById(usuario.getIdTipoDoc()).orElse(null);
        Direccione direccion = direccioneRepository.findById(usuario.getIdDireccion()).orElse(null);

        Usuario nuevo = new Usuario();
        nuevo.setIdauth(auth);
        nuevo.setIdtipodoc(tipo);
        nuevo.setIddireccion(direccion);
        nuevo.setNacimiento(usuario.getNacimiento());
        nuevo.setDocumento(usuario.getDocumento());
        nuevo.setTelefono(usuario.getTelefono());

        usuarioRepository.save(nuevo);

        return AuthResponse.builder()
                .token(jwtService.getToken(auth))
                .build();
    }
}
