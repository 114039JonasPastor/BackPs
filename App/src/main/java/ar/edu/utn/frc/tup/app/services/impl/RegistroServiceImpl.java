package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.auth.AuthResponse;
import ar.edu.utn.frc.tup.app.auth.services.JwtService;
import ar.edu.utn.frc.tup.app.dtos.request.registro.UsuarioRequest;
import ar.edu.utn.frc.tup.app.entities.*;
import ar.edu.utn.frc.tup.app.repositories.*;
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
    BarrioRepository barrioRepository;

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
        Barrio barrio = barrioRepository.findById(usuario.getIdBarrio()).orElse(null);

        Direccione direccion = new Direccione();
        direccion.setIdbarrio(barrio);
        direccion.setCalle(usuario.getCalle());
        direccion.setNumero(usuario.getNumero());
        direccion.setDepto(usuario.getDepto().isPresent() ? usuario.getDepto().get() : null);
        direccion.setPiso(usuario.getPiso().isPresent() ? usuario.getPiso().get() : null);
        direccion.setObservaciones(usuario.getObservaciones().isPresent() ? usuario.getObservaciones().get() : null);

        Direccione direccionSaved = direccioneRepository.save(direccion);

        Usuario nuevo = new Usuario();
        nuevo.setIdauth(auth);
        nuevo.setIdtipodoc(tipo);
        nuevo.setIddireccion(direccionSaved);
        nuevo.setNacimiento(usuario.getNacimiento());
        nuevo.setDocumento(usuario.getDocumento());
        nuevo.setTelefono(usuario.getTelefono());

        usuarioRepository.save(nuevo);

        return AuthResponse.builder()
                .token(jwtService.getToken(auth))
                .build();
    }
}
