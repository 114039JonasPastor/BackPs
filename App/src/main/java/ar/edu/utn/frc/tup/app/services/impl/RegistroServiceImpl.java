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
import ar.edu.utn.frc.tup.app.services.StreamChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
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

    // ✅ Agregar dependencia de StreamChatService
    private final StreamChatService streamChatService;

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
                    .active(false)
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

            // ✅ REGISTRAR USUARIO EN STREAM CHAT
            try {
                String userId = String.valueOf(nuevo.getId());
                String nombre = auth.getName() + " " + auth.getLastname();
                String email = auth.getMail();

                streamChatService.createOrUpdateUser(userId, nombre, email, null);
                log.info("✅ Usuario registrado en Stream Chat: {}", userId);
            } catch (Exception e) {
                log.error("⚠️ Error al registrar usuario en Stream Chat (continuando): {}", e.getMessage());
                // No lanzar excepción para no interrumpir el registro
            }

            // Generar token de confirmación y enviar mail
            String token = confirmationTokenService.createTokenForAuth(auth.getId());
            String confirmationLink = "http://localhost:8081/api/v1/registro/confirm?token=" + token;

            String htmlBody = loadAndProcessEmailTemplate(auth.getName(), auth.getLastname(), confirmationLink);
            emailService.sendHtml(auth.getMail(), "Confirma tu cuenta - Tu Oficio", htmlBody);

            return AuthResponse.builder()
                    .token(null)
                    .nombre(auth.getName())
                    .apellido(auth.getLastname())
                    .email(auth.getMail())
                    .idUsuario(nuevo != null ? nuevo.getId() : null)
                    .documento(nuevo != null ? nuevo.getDocumento() : null)
                    .telefono(nuevo != null ? nuevo.getTelefono() : null)
                    .nacimiento(nuevo != null && nuevo.getNacimiento() != null ? nuevo.getNacimiento().toString() : null)
                    .idDireccion(nuevo != null ? nuevo.getIddireccion().getId() : null)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error durante el registro del usuario: " + e.getMessage(), e);
        }
    }

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

            if(profesionalRepository.findByIdusuario_Id(usuario.getId()).isEmpty()){
                profesionalRepository.save(profesional);

                // ✅ ACTUALIZAR USUARIO EN STREAM CHAT COMO PROFESIONAL
                try {
                    String userId = String.valueOf(usuario.getId());
                    String nombre = usuario.getIdauth().getName() + " " + usuario.getIdauth().getLastname() + " (Profesional)";
                    String email = usuario.getIdauth().getMail();

                    streamChatService.createOrUpdateUser(userId, nombre, email, null);
                    log.info("✅ Profesional actualizado en Stream Chat: {}", userId);
                } catch (Exception e) {
                    log.error("⚠️ Error al actualizar profesional en Stream Chat (continuando): {}", e.getMessage());
                }

                return profesional;
            } else{
                throw new RuntimeException("Este usuario ya es un profesional registrado");
            }

        } catch (Exception e){
            throw new RuntimeException("Error durante el registro del profesional: " + e.getMessage(), e);
        }
    }

    private String loadAndProcessEmailTemplate(String nombre, String apellido, String confirmationLink) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/email-confirmation.html");
            String htmlTemplate = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            return htmlTemplate
                    .replace("{{nombre}}", nombre != null ? nombre : "")
                    .replace("{{apellido}}", apellido != null ? apellido : "")
                    .replace("{{confirmationLink}}", confirmationLink);

        } catch (IOException e) {
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

