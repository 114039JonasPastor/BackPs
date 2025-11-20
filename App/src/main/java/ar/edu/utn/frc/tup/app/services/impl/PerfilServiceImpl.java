package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.dtos.DomicilioDto;
import ar.edu.utn.frc.tup.app.dtos.request.perfil.ModificarCliente;
import ar.edu.utn.frc.tup.app.dtos.request.perfil.ModificarProfesional;
import ar.edu.utn.frc.tup.app.dtos.response.perfil.PerfilCliente;
import ar.edu.utn.frc.tup.app.dtos.response.perfil.PerfilProfesional;
import ar.edu.utn.frc.tup.app.dtos.response.perfil.metrica.ProfesionalMetrica;
import ar.edu.utn.frc.tup.app.dtos.response.perfil.metrica.UsuarioMetrica;
import ar.edu.utn.frc.tup.app.entities.*;
import ar.edu.utn.frc.tup.app.repositories.*;
import ar.edu.utn.frc.tup.app.services.PerfilService;
import ar.edu.utn.frc.tup.app.services.ReseniaService;
import ar.edu.utn.frc.tup.app.services.TrabajoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PerfilServiceImpl implements PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final AuthRepository authRepository;
    private final DireccionRepository direccionRepository;
    private final CiudadRepository ciudadRepository;
    private final DepartamentoRepository departamentoRepository;
    private final BarrioRepository barrioRepository;
    private final ProfesionalRepository professionelleRepository;
    private final MontoRepository montoRepository;
    private final DisponibilidadRepository disponibilidadRepository;
    private final EspecialidadRepository especialidadRepository;
    private final OficioRepository oficioRepository;

    private final ReseniaService reseniaService;
    private final TrabajoService trabajoService;

    @Override
    public PerfilCliente getPerfilCliente(Integer idCliente) {
        Usuario usuario = usuarioRepository.findById(idCliente).orElse(null);
        if(usuario != null){
            Direccione direccion = usuario.getIddireccion();
            DomicilioDto domicilioDto = new DomicilioDto();
            if (direccion != null) {
                var barrio = barrioRepository.findById(direccion.getIdbarrio().getId()).orElse(null);
                var ciudad = ciudadRepository.findById(barrio.getIdciudad().getId()).orElse(null);
                var departamento = departamentoRepository.findById(ciudad.getIddepartamento().getId()).orElse(null);

                domicilioDto.setCalle(direccion.getCalle());
                domicilioDto.setNumero(direccion.getNumero());
                domicilioDto.setPiso(direccion.getPiso());
                domicilioDto.setDepto(direccion.getDepto());
                domicilioDto.setBarrio(barrio != null ? barrio.getBarrio() : null);
                domicilioDto.setCiudad(ciudad != null ? ciudad.getCiudad() : null);
                domicilioDto.setDepartamento(departamento != null ? departamento.getDepartamento() : null);
            }
            var tipoDocumento = usuario.getIdtipodoc() != null ? usuario.getIdtipodoc().getTipo() : null;

            PerfilCliente perfil = PerfilCliente.builder()
                    .name(usuario.getIdauth().getName())
                    .lastName(usuario.getIdauth().getLastname())
                    .telefono(usuario.getTelefono())
                    .tipoDocumento(tipoDocumento)
                    .documento(usuario.getDocumento())
                    .nacimiento(usuario.getNacimiento())
                    .email(usuario.getIdauth().getUsername())
                    .domicilio(domicilioDto)
                    .build();
            return perfil;
        } else {
            throw new RuntimeException("Usuario no encontrado");
        }
    }

    @Override
    public PerfilCliente updatePerfilCliente(ModificarCliente cliente) {
        Auth auth = authRepository.findByMail(cliente.getMail()).orElse(null);
        Direccione direccion = direccionRepository.findById(cliente.getAdress().getId()).orElse(null);

        if(auth == null){
            throw new RuntimeException("Usuario no encontrado");
        }

        auth.setName(cliente.getName());
        auth.setLastname(cliente.getLastName());
        authRepository.save(auth);

        direccion.setIdbarrio(cliente.getAdress().getIdbarrio());
        direccion.setCalle(cliente.getAdress().getCalle());
        direccion.setNumero(cliente.getAdress().getNumero());
        direccion.setPiso(cliente.getAdress().getPiso());
        direccion.setDepto(cliente.getAdress().getDepto());
        direccion.setObservaciones(cliente.getAdress().getObservaciones());

        direccionRepository.save(direccion);

        Usuario usuario = usuarioRepository.findByIdauth(auth).orElse(null);
        if(usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }
        usuario.setTelefono(cliente.getPhone());
        usuario.setIddireccion(direccion);

        usuarioRepository.save(usuario);

        return PerfilCliente.builder()
                .name(auth.getName())
                .lastName(auth.getLastname())
                .email(auth.getUsername())
                .build();
    }

    @Override
    public PerfilProfesional getPerfilProfesional(Integer idProfesional) {
        Profesionale profesional = professionelleRepository.findById(idProfesional).orElse(null);
        if(profesional == null){
            throw new RuntimeException("Profesional no encontrado");
        }

        Monto monto = montoRepository.findByIdprofesional_Id(profesional.getId()).orElse(null);

        String rangoPrecio = calcularRangoPrecio(monto, profesional);
        List<String> especialidadesList = obtenerEspecialidades(profesional);

        return PerfilProfesional.builder()
                .idProfesional(profesional.getId())
                .nombre(profesional.getIdusuario().getIdauth().getName())
                .apellido(profesional.getIdusuario().getIdauth().getLastname())
                .oficio(profesional.getIdoficio().getOficio())
                .telefono(profesional.getIdusuario().getTelefono())
                .rangoPrecio(rangoPrecio)
                .especialidades(especialidadesList)
                .build();
    }

    @Override
    public PerfilProfesional updatePerfilProfesional(ModificarProfesional request) {
        // Buscar el profesional por ID
        Profesionale profesional = professionelleRepository.findById(request.getIdProfesional())
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado"));

        // Actualizar oficio si se proporciona
        if (request.getIdOficio() != null) {
            Oficio oficio = oficioRepository.findById(request.getIdOficio())
                    .orElseThrow(() -> new RuntimeException("Oficio no encontrado"));
            profesional.setIdoficio(oficio);
        }

        // Actualizar fechas de vigencia
        if (request.getFechaDesde() != null) {
            profesional.setFechadesde(request.getFechaDesde());
        }
        if (request.getFechaHasta() != null) {
            profesional.setFechahasta(request.getFechaHasta());
        }

        // Actualizar precios
        if (request.getPrecioMin() != null) {
            profesional.setPrecioMin(request.getPrecioMin());
        }
        if (request.getPrecioMax() != null) {
            profesional.setPrecioMax(request.getPrecioMax());
        }

        professionelleRepository.save(profesional);

        // Actualizar las especialidades
        if (request.getEspecialidades() != null) {
            // Eliminar las especialidades existentes
            if (profesional.getEspecialidades() != null && !profesional.getEspecialidades().isEmpty()) {
                especialidadRepository.deleteAll(profesional.getEspecialidades());
            }

            // Agregar las nuevas especialidades
            final Profesionale profesionalFinal = profesional;
            request.getEspecialidades().forEach(especialidadNombre -> {
                Especialidad especialidad = Especialidad.builder()
                        .especialidad(especialidadNombre)
                        .idprofesional(profesionalFinal)
                        .build();
                especialidadRepository.save(especialidad);
            });
        }

        // Recargar el profesional con las especialidades actualizadas
        profesional = professionelleRepository.findById(request.getIdProfesional())
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado"));

        return mapToPerfilProfesional(profesional);
    }

    @Override
    public void updateAvatar(Integer idAuth, String avatarUrl) {
        Auth auth = authRepository.findById(idAuth).orElse(null);
        Usuario usuario = usuarioRepository.findByIdauth(auth).orElse(null);

        usuario.setAvatar(avatarUrl);

        usuarioRepository.save(usuario);
    }

    @Override
    public String getAvatar(Integer idAuth) {
        Auth auth = authRepository.findById(idAuth).orElse(null);
        Usuario usuario = usuarioRepository.findByIdauth(auth).orElse(null);

        return usuario.getAvatar();
    }

    @Override
    @Transactional
    public List<PerfilProfesional> getProfesionalesByOficio(String oficio) {
        try {
            List<Profesionale> profesionales = professionelleRepository.findByOficioSimple(oficio);

            // Forzar la carga de especialidades dentro de la transacción
            profesionales.forEach(p -> {
                if (p.getEspecialidades() != null) {
                    p.getEspecialidades().size(); // Esto fuerza la carga lazy
                }
            });

            return profesionales.stream()
                    .map(this::mapToPerfilProfesional)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener profesionales por oficio: " + e.getMessage(), e);
        }
    }

    @Override
    public void agregarStrike(Integer idUsuario, String motivo) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Integer strikesActuales = usuario.getStrike() != null ? usuario.getStrike() : 0;
        usuario.setStrike(strikesActuales + 1);

        // Si llega a 3 strikes, suspender usuario
        if (usuario.getStrike() >= 3) {
            usuario.getIdauth().setActive(false);
            // Enviar notificación, etc.
        }
        usuarioRepository.save(usuario);
    }

    @Override
    public List<UsuarioMetrica> getUsuariosMetrica() {
        List<Usuario> usuarios = usuarioRepository.findAll();

        List<UsuarioMetrica> usuarioMetricas = new ArrayList<>();

        for (Usuario usuario : usuarios) {
            UsuarioMetrica metrica = UsuarioMetrica.builder()
                    .nombre(usuario.getIdauth().getName() + " " + usuario.getIdauth().getLastname())
                    .email(usuario.getIdauth().getMail())
                    .strikes(usuario.getStrike() != null ? usuario.getStrike() : 0)
                    .estado(usuario.getIdauth().getActive())
                    .build();
            usuarioMetricas.add(metrica);
        }

        return usuarioMetricas;
    }

    @Override
    public List<ProfesionalMetrica> getProfesionalesMetrica() {
        List<Profesionale> profesionales = professionelleRepository.findAll();

        List<ProfesionalMetrica> profesionalMetricas = new ArrayList<>();

        for(Profesionale profesional : profesionales) {
            String calificacionTexto = "No tiene reseñas";
            Integer serviciosCompletados = 0;

            try {
                Integer calificacionNumerica = reseniaService.getPromedioProfesional(profesional.getId()).getPuntuacion().intValue();

                calificacionTexto = calificacionNumerica.toString();

                serviciosCompletados = trabajoService.obtenerTrabajosPorUsuario(profesional.getIdusuario().getId(),"FINALIZADO").size();

            } catch (Exception e) {
                System.err.println("Error al obtener promedio o trabajos para el profesional " + profesional.getId() + ": " + e.getMessage());
            }

            ProfesionalMetrica metrica = ProfesionalMetrica.builder()
                    .nombre(profesional.getIdusuario().getIdauth().getName() + " " +
                            profesional.getIdusuario().getIdauth().getLastname())
                    .oficio(profesional.getIdoficio().getOficio())
                    .calificacion(calificacionTexto)
                    .serviciosCompletados(serviciosCompletados)
                    .build();
            profesionalMetricas.add(metrica);
        }

        return profesionalMetricas;
    }

    // ==================== MÉTODOS PRIVADOS DE APOYO ====================

    /**
     * Mapea un Profesionale a PerfilProfesional
     */
    private PerfilProfesional mapToPerfilProfesional(Profesionale profesional) {
        Monto monto = montoRepository.findByIdprofesional_Id(profesional.getId()).orElse(null);

        String rangoPrecio = calcularRangoPrecio(monto, profesional);
        List<String> especialidadesList = obtenerEspecialidades(profesional);

        return PerfilProfesional.builder()
                .idProfesional(profesional.getId())
                .nombre(profesional.getIdusuario().getIdauth().getName())
                .apellido(profesional.getIdusuario().getIdauth().getLastname())
                .oficio(profesional.getIdoficio().getOficio())
                .telefono(profesional.getIdusuario().getTelefono())
                .rangoPrecio(rangoPrecio)
                .especialidades(especialidadesList)
                .build();
    }

    /**
     * Calcula el rango de precio del profesional
     */
    private String calcularRangoPrecio(Monto monto, Profesionale profesional) {
        if (monto != null && monto.getPreciomin() != null && monto.getPreciomax() != null) {
            return monto.getPreciomin() + " - " + monto.getPreciomax();
        } else if (profesional.getPrecioMin() != null && profesional.getPrecioMax() != null) {
            return profesional.getPrecioMin() + " - " + profesional.getPrecioMax();
        }
        return "No especificado";
    }

    /**
     * Obtiene la lista de especialidades del profesional
     */
    private List<String> obtenerEspecialidades(Profesionale profesional) {
        if (profesional.getEspecialidades() != null) {
            try {
                return profesional.getEspecialidades().stream()
                        .map(Especialidad::getEspecialidad)
                        .toList();
            } catch (Exception e) {
                // Si falla la carga lazy, retornar lista vacía
                return List.of();
            }
        }
        return List.of();
    }
}
