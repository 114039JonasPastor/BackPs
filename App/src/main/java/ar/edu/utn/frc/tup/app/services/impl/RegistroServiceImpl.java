package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.auth.AuthResponse;
import ar.edu.utn.frc.tup.app.auth.services.JwtService;
import ar.edu.utn.frc.tup.app.dtos.request.registro.UsuarioRequest;
import ar.edu.utn.frc.tup.app.entities.*;
import ar.edu.utn.frc.tup.app.repositories.*;
import ar.edu.utn.frc.tup.app.services.RegistroService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistroServiceImpl implements RegistroService {

    private final AuthRepository authRepository;

    private final UsuarioRepository usuarioRepository;

    private final TipoDocumentoRepository tipoDocumentoRepository;

    private final DireccionRepository direccioneRepository;

    private final BarrioRepository barrioRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse registrarUsuario(UsuarioRequest usuario) { //TODO revisar
        try {
            // Validar que los datos requeridos existan
            TiposDocumento tipo = tipoDocumentoRepository.findById(usuario.getIdTipoDoc())
                    .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado"));
            Barrio barrio = barrioRepository.findById(usuario.getIdBarrio())
                    .orElseThrow(() -> new RuntimeException("Barrio no encontrado"));

            // Crear y guardar la autenticación
            Auth auth = Auth.builder()
                    .password(passwordEncoder.encode(usuario.getPassword()))
                    .mail(usuario.getMail())
                    .name(usuario.getName())
                    .lastname(usuario.getLastName())
                    .active(true) //Todo revisar
                    .build();
            authRepository.save(auth);

            // Crear y guardar la dirección
            Direccione direccion = new Direccione();
            direccion.setIdbarrio(barrio);
            direccion.setCalle(usuario.getCalle());
            direccion.setNumero(usuario.getNumero());
            direccion.setDepto(usuario.getDepto().isPresent() ? usuario.getDepto().get() : null);
            direccion.setPiso(usuario.getPiso().isPresent() ? usuario.getPiso().get() : null);
            direccion.setObservaciones(usuario.getObservaciones().isPresent() ? usuario.getObservaciones().get() : null);

            Direccione direccionSaved = direccioneRepository.save(direccion);

            // Crear y guardar el usuario
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
                    
        } catch (Exception e) {
            // El rollback se maneja automáticamente por @Transactional
            throw new RuntimeException("Error durante el registro del usuario: " + e.getMessage(), e);
        }
    }
}
