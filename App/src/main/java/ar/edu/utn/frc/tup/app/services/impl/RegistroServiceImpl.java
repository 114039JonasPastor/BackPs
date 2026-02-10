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
import java.util.List;
import java.util.stream.Collectors;

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
    private final StreamChatService streamChatService;
    private final RoleRepository roleRepository;
    private final RolxusuarioRepository rolxusuarioRepository;

    @Override
    @Transactional
    public AuthResponse registrarUsuario(UsuarioRequest usuario) {
        try {
            log.info("🔵 Iniciando registro de usuario: {}", usuario.getMail());
            
            // Check if email already exists
            if (authRepository.findByMail(usuario.getMail()).isPresent()) {
                log.warn("⚠️ Email ya registrado: {}", usuario.getMail());
                throw new IllegalArgumentException("El email ya está registrado");
            }
            
            log.info("ID Tipo Documento recibido: {}", usuario.getIdTipoDoc());
            TiposDocumento tipo = tipoDocumentoRepository.findById(usuario.getIdTipoDoc())
                .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado"));
            log.info("✅ Tipo documento encontrado");
            
            Barrio barrio = barrioRepository.findById(usuario.getIdBarrio())
                .orElseThrow(() -> new RuntimeException("Barrio no encontrado"));
            log.info("✅ Barrio encontrado");

            Auth auth = Auth.builder()
                    .password(passwordEncoder.encode(usuario.getPassword()))
                    .mail(usuario.getMail())
                    .name(usuario.getName())
                    .lastname(usuario.getLastName())
                    .active(false)
                    .build();
            authRepository.save(auth);
            log.info("✅ Auth guardado con ID: {}", auth.getId());

            Direccione direccion = new Direccione();
            direccion.setIdbarrio(barrio);
            direccion.setCalle(usuario.getCalle());
            direccion.setNumero(usuario.getNumero());
            direccion.setDepto(usuario.getDepto() != null && usuario.getDepto().isPresent() ? usuario.getDepto().get() : null);
            direccion.setPiso(usuario.getPiso() != null && usuario.getPiso().isPresent() ? usuario.getPiso().get() : null);
            direccion.setObservaciones(usuario.getObservaciones() != null && usuario.getObservaciones().isPresent() ? usuario.getObservaciones().get() : null);

            Direccione direccionSaved = direccioneRepository.save(direccion);
            log.info("✅ Dirección guardada con ID: {}", direccionSaved.getId());

            Usuario nuevo = new Usuario();
            nuevo.setIdauth(auth);
            nuevo.setIdtipodoc(tipo);
            nuevo.setIddireccion(direccionSaved);
            nuevo.setNacimiento(usuario.getNacimiento());
            nuevo.setDocumento(usuario.getDocumento());
            nuevo.setTelefono(usuario.getTelefono());

            usuarioRepository.save(nuevo);
            log.info("✅ Usuario guardado con ID: {}", nuevo.getId());

            Role rolCliente = roleRepository.findByDescripcion("CLIENTE")
                    .orElseThrow(() -> new RuntimeException("Rol CLIENTE no encontrado"));

            if (!rolxusuarioRepository.existsByIdauthAndIdrol(auth, rolCliente)) {
                Rolxusuario rolxusuario = new Rolxusuario();
                rolxusuario.setIdauth(auth);
                rolxusuario.setIdrol(rolCliente);
                rolxusuarioRepository.save(rolxusuario);
                log.info("✅ Rol CLIENTE asignado");
            }

            try {
                String userId = String.valueOf(nuevo.getId());
                String nombre = auth.getName() + " " + auth.getLastname();
                String email = auth.getMail();

                streamChatService.createOrUpdateUser(userId, nombre, email, null);
                log.info("Usuario registrado en Stream Chat: {}", userId);
            } catch (Exception e) {
                log.error("Error al registrar usuario en Stream Chat (continuando): {}", e.getMessage());
            }

            String token = confirmationTokenService.createTokenForAuth(auth.getId());
            String baseUrl = System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");
            String confirmationLink = baseUrl + "/api/v1/registro/confirm?token=" + token;
            log.info("✅ Token de confirmación creado. Link: {}", confirmationLink);

            boolean emailSent = false;
            try {
                String htmlBody = loadAndProcessEmailTemplate(auth.getName(), auth.getLastname(), confirmationLink);
                log.info("📧 Enviando email de confirmación a: {}", auth.getMail());
                log.info("📧 Link de confirmación: {}", confirmationLink);
                emailService.sendHtml(auth.getMail(), "Confirma tu cuenta - Tu Oficio", htmlBody);
                log.info("✅ Email enviado exitosamente");
                emailSent = true;
            } catch (Exception emailError) {
                log.error("⚠️⚠️⚠️ ERROR CRÍTICO: No se pudo enviar el email de confirmación ⚠️⚠️⚠️");
                log.error("Usuario: {}", auth.getMail());
                log.error("Tipo de error: {}", emailError.getClass().getName());
                log.error("Mensaje de error: {}", emailError.getMessage());
                log.error("Stack trace:", emailError);
                log.error("⚠️ ACCIÓN REQUERIDA: Revisar configuración de EMAIL_USERNAME y EMAIL_PASSWORD en Render");
                log.error("⚠️ Link de confirmación para uso manual: {}", confirmationLink);
                // Continue with registration even if email fails
            }
            
            if (!emailSent) {
                log.warn("⚠️ ADVERTENCIA: Usuario registrado pero SIN recibir email de confirmación");
                log.warn("⚠️ Email: {}", auth.getMail());
                log.warn("⚠️ La cuenta está INACTIVA y no podrá iniciar sesión hasta confirmar");
            }

            AuthResponse response = AuthResponse.builder()
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
            
            log.info("✅ Registro completado exitosamente para usuario: {}", auth.getMail());
            return response;

        } catch (Exception e) {
            log.error("❌ Error durante el registro del usuario: {}", e.getMessage(), e);
            throw new RuntimeException("Error durante el registro del usuario: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
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

                Auth auth = usuario.getIdauth();
                Role rolProfesional = roleRepository.findByDescripcion("PROFESIONAL")
                        .orElseThrow(() -> new RuntimeException("Rol PROFESIONAL no encontrado"));

                if (!rolxusuarioRepository.existsByIdauthAndIdrol(auth, rolProfesional)) {
                    Rolxusuario rolxusuario = new Rolxusuario();
                    rolxusuario.setIdauth(auth);
                    rolxusuario.setIdrol(rolProfesional);
                    rolxusuarioRepository.save(rolxusuario);
                }

                try {
                    String userId = String.valueOf(usuario.getId());
                    String nombre = usuario.getIdauth().getName() + " " + usuario.getIdauth().getLastname() + " (Profesional)";
                    String email = usuario.getIdauth().getMail();

                    streamChatService.createOrUpdateUser(userId, nombre, email, null);
                    log.info("Profesional actualizado en Stream Chat: {}", userId);
                } catch (Exception e) {
                    log.error("Error al actualizar profesional en Stream Chat (continuando): {}", e.getMessage());
                }

                return profesional;
            } else{
                throw new RuntimeException("Este usuario ya es un profesional registrado");
            }

        } catch (Exception e) {
            throw new RuntimeException("Error durante el registro del profesional: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public AuthResponse registrarAdministrador(UsuarioRequest adminRequest) {
        try {
            TiposDocumento tipo = tipoDocumentoRepository.findById(adminRequest.getIdTipoDoc())
                    .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado"));
            Barrio barrio = barrioRepository.findById(adminRequest.getIdBarrio())
                    .orElseThrow(() -> new RuntimeException("Barrio no encontrado"));

            Auth auth = Auth.builder()
                    .password(passwordEncoder.encode(adminRequest.getPassword()))
                    .mail(adminRequest.getMail())
                    .name(adminRequest.getName())
                    .lastname(adminRequest.getLastName())
                    .active(true)
                    .build();
            authRepository.save(auth);

            Role rolAdmin = roleRepository.findByDescripcion("ADMINISTRADOR")
                    .orElseThrow(() -> new RuntimeException("Rol ADMINISTRADOR no encontrado"));

            if (!rolxusuarioRepository.existsByIdauthAndIdrol(auth, rolAdmin)) {
                Rolxusuario rolxusuario = new Rolxusuario();
                rolxusuario.setIdauth(auth);
                rolxusuario.setIdrol(rolAdmin);
                rolxusuarioRepository.save(rolxusuario);
            }

            String jwtToken = jwtService.getToken(auth);

            List<String> roles = rolxusuarioRepository.findByIdauth(auth).stream()
                    .map(rolxusuario -> rolxusuario.getIdrol().getDescripcion())
                    .collect(Collectors.toList());

            return AuthResponse.builder()
                    .token(jwtToken)
                    .nombre(auth.getName())
                    .apellido(auth.getLastname())
                    .email(auth.getMail())
                    .idUsuario(null)
                    .documento(null)
                    .telefono(null)
                    .nacimiento(null)
                    .idDireccion(null)
                    .roles(roles)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error durante el registro del administrador: " + e.getMessage(), e);
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

    @Override
    @Transactional
    public void reenviarEmailConfirmacion(String email) {
        try {
            log.info("🔄 Solicitud de reenvío de email de confirmación para: {}", email);
            
            Auth auth = authRepository.findByMail(email)
                    .orElseThrow(() -> new RuntimeException("No se encontró ninguna cuenta con ese correo electrónico"));
            
            if (auth.getActive()) {
                throw new RuntimeException("Esta cuenta ya está verificada. Puedes iniciar sesión.");
            }
            
            String token = confirmationTokenService.createTokenForAuth(auth.getId());
            String baseUrl = System.getenv().getOrDefault("BACKEND_URL", "http://localhost:8081");
            String confirmationLink = baseUrl + "/api/v1/registro/confirm?token=" + token;
            
            String htmlBody = loadAndProcessEmailTemplate(auth.getName(), auth.getLastname(), confirmationLink);
            log.info("📧 Reenviando email de confirmación a: {}", auth.getMail());
            emailService.sendHtml(auth.getMail(), "Confirma tu cuenta - Tu Oficio", htmlBody);
            log.info("✅ Email reenviado exitosamente");
            
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Error reenviando email de confirmación: {}", e.getMessage(), e);
            throw new RuntimeException("Error al reenviar el email de confirmación: " + e.getMessage(), e);
        }
    }
}
