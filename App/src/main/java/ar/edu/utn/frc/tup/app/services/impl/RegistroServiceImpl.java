package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.auth.AuthResponse;
import ar.edu.utn.frc.tup.app.auth.services.JwtService;
import ar.edu.utn.frc.tup.app.dtos.request.registro.ProfesionalRequest;
import ar.edu.utn.frc.tup.app.dtos.request.registro.UsuarioRequest;
import ar.edu.utn.frc.tup.app.entities.*;
import ar.edu.utn.frc.tup.app.repositories.*;
import ar.edu.utn.frc.tup.app.services.ConfirmationTokenService;
import ar.edu.utn.frc.tup.app.services.EmailService;
import ar.edu.utn.frc.tup.app.services.RegistroService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class RegistroServiceImpl implements RegistroService {

    private final AuthRepository authRepository;

    private final UsuarioRepository usuarioRepository;

    private final ProfesionalRepository profesionalRepository;

    private final OficioRepository oficioRepository;

    private final TipoDocumentoRepository tipoDocumentoRepository;

    private final DireccionRepository direccioneRepository;

    private final BarrioRepository barrioRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final EmailService emailService;

    private final ConfirmationTokenService confirmationTokenService;

    @Override
    @Transactional
    public AuthResponse registrarUsuario(UsuarioRequest usuario) {
        try {
            TiposDocumento tipo = tipoDocumentoRepository.findById(usuario.getIdTipoDoc())
                    .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado"));
            Barrio barrio = barrioRepository.findById(usuario.getIdBarrio())
                    .orElseThrow(() -> new RuntimeException("Barrio no encontrado"));

            // Crear y guardar la autenticación pero inactiva hasta confirmar
            Auth auth = Auth.builder()
                    .password(passwordEncoder.encode(usuario.getPassword()))
                    .mail(usuario.getMail())
                    .name(usuario.getName())
                    .lastname(usuario.getLastName())
                    .active(false) // inactivo hasta confirmar
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

            // Generar token de confirmación y enviar mail
            String token = confirmationTokenService.createTokenForAuth(auth.getId());
            String confirmationLink = "http://localhost:8081/api/v1/registro/confirm?token=" + token;

            // Cargar y procesar template HTML
            String htmlBody = loadAndProcessEmailTemplate(auth.getName(), auth.getLastname(), confirmationLink);
            emailService.sendHtml(auth.getMail(), "Confirma tu cuenta - Servicios Pro", htmlBody);

            // No devolver JWT hasta confirmación
            return AuthResponse.builder().token(null).build();

        } catch (Exception e) {
            throw new RuntimeException("Error durante el registro del usuario: " + e.getMessage(), e);
        }
    }


    //Fixme Posible error en el que el usuario se puede registrar como profesional muchas veces
    @Override
    public Profesionale registrarProfesional(ProfesionalRequest profesionalRequest) {
        try {
            Usuario usuario = usuarioRepository.findById(profesionalRequest.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Oficio oficio = oficioRepository.findById(profesionalRequest.getIdOficio())
                    .orElseThrow(() -> new RuntimeException("Oficio no encontrado"));

            Profesionale profesional = Profesionale.builder()
                    .idusuario(usuario)
                    .idoficio(oficio)
                    .fechadesde(profesionalRequest.getFechaDesde())
                    .fechahasta(null)
                    .build();

            return profesionalRepository.save(profesional);

        } catch (Exception e){
            throw new RuntimeException("Error durante el registro del profesional: " + e.getMessage(), e);
        }
    }

    private String loadAndProcessEmailTemplate(String nombre, String apellido, String confirmationLink) {
        try {
            // Cargar el template HTML desde resources
            ClassPathResource resource = new ClassPathResource("templates/email-confirmation.html");
            String htmlTemplate = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            // Reemplazar los placeholders con los datos reales
            return htmlTemplate
                    .replace("{{nombre}}", nombre != null ? nombre : "")
                    .replace("{{apellido}}", apellido != null ? apellido : "")
                    .replace("{{confirmationLink}}", confirmationLink);

        } catch (IOException e) {
            // Fallback a template simple en caso de error
            return createFallbackTemplate(nombre, apellido, confirmationLink);
        }
    }

    private String createFallbackTemplate(String nombre, String apellido, String confirmationLink) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background-color: #f8f9fa; padding: 30px; border-radius: 10px;">
                        <h2 style="color: #27ae60; text-align: center;">¡Bienvenido/a a Servicios Pro!</h2>
                        <p>Hola <strong>%s %s</strong>,</p>
                        <p>Gracias por registrarte en nuestra plataforma. Para completar tu registro, confirma tu cuenta haciendo clic en el siguiente enlace:</p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="background-color: #27ae60; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; font-weight: bold;">Confirmar mi cuenta</a>
                        </div>
                        <p style="font-size: 12px; color: #666;">Si no te registraste en nuestra plataforma, puedes ignorar este correo.</p>
                    </div>
                </body>
                </html>
                """.formatted(
                    nombre != null ? nombre : "",
                    apellido != null ? apellido : "",
                    confirmationLink
                );
    }
}
