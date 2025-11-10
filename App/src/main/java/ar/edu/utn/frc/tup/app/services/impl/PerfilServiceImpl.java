package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.dtos.DomicilioDto;
import ar.edu.utn.frc.tup.app.dtos.request.perfil.ModificarCliente;
import ar.edu.utn.frc.tup.app.dtos.request.perfil.ModificarProfesional;
import ar.edu.utn.frc.tup.app.dtos.response.PerfilCliente;
import ar.edu.utn.frc.tup.app.dtos.response.PerfilProfesional;
import ar.edu.utn.frc.tup.app.entities.*;
import ar.edu.utn.frc.tup.app.entities.Auth;
import ar.edu.utn.frc.tup.app.entities.Direccione;
import ar.edu.utn.frc.tup.app.entities.Usuario;
import ar.edu.utn.frc.tup.app.repositories.*;
import ar.edu.utn.frc.tup.app.services.PerfilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

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
        Disponibilidad disponibilidad = disponibilidadRepository.findByIdprofesional_Id(profesional.getId()).orElse(null);
        Monto monto = montoRepository.findByIdprofesional_Id(profesional.getId()).orElse(null);

        // Handle rangoPrecio with null check for monto
        String rangoPrecio;
        if(monto != null && monto.getPreciomin() != null && monto.getPreciomax() != null) {
            rangoPrecio = monto.getPreciomin().toString() + " - " + monto.getPreciomax().toString();
        } else if(profesional.getPrecioMin() != null && profesional.getPrecioMax() != null) {
            rangoPrecio = profesional.getPrecioMin().toString() + " - " + profesional.getPrecioMax().toString();
        } else {
            rangoPrecio = "No especificado";
        }

        // Handle disponibilidad with null check
        String diaDisponible;
        if(disponibilidad != null && disponibilidad.getDiasemana() != null &&
           disponibilidad.getHorainicio() != null && disponibilidad.getHorafin() != null) {
            //Fixme mejorar el manejo de horario(puede que haya que cambiarlo desde la base de datos)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            diaDisponible = disponibilidad.getDiasemana() + " de " +
                    disponibilidad.getHorainicio().format(formatter) + " a " +
                    disponibilidad.getHorafin().format(formatter);
        } else {
            diaDisponible = "No especificada";
        }

        // Extract especialidades
        List<String> especialidadesList = profesional.getEspecialidades() != null
                ? profesional.getEspecialidades().stream()
                    .map(Especialidad::getEspecialidad)
                    .toList()
                : List.of();

        return PerfilProfesional.builder()
                .nombre(profesional.getIdusuario().getIdauth().getName())
                .apellido(profesional.getIdusuario().getIdauth().getLastname())
                .oficio(profesional.getIdoficio().getOficio())
                .telefono(profesional.getIdusuario().getTelefono())
                .rangoPrecio(rangoPrecio)
                .disponibilidad(diaDisponible)
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

        // Construir y retornar el perfil actualizado
        Disponibilidad disponibilidad = disponibilidadRepository.findByIdprofesional_Id(profesional.getId()).orElse(null);
        Monto monto = montoRepository.findByIdprofesional_Id(profesional.getId()).orElse(null);

        // Handle rangoPrecio with null check for monto
        String rangoPrecio;
        if(monto != null && monto.getPreciomin() != null && monto.getPreciomax() != null) {
            rangoPrecio = monto.getPreciomin().toString() + " - " + monto.getPreciomax().toString();
        } else if(profesional.getPrecioMin() != null && profesional.getPrecioMax() != null) {
            rangoPrecio = profesional.getPrecioMin().toString() + " - " + profesional.getPrecioMax().toString();
        } else {
            rangoPrecio = "No especificado";
        }

        // Handle disponibilidad with null check
        String diaDisponible;
        if(disponibilidad != null && disponibilidad.getDiasemana() != null &&
           disponibilidad.getHorainicio() != null && disponibilidad.getHorafin() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            diaDisponible = disponibilidad.getDiasemana() + " de " +
                    disponibilidad.getHorainicio().format(formatter) + " a " +
                    disponibilidad.getHorafin().format(formatter);
        } else {
            diaDisponible = "No especificada";
        }

        // Extract especialidades
        List<String> especialidadesList = profesional.getEspecialidades() != null
                ? profesional.getEspecialidades().stream()
                    .map(Especialidad::getEspecialidad)
                    .toList()
                : List.of();

        return PerfilProfesional.builder()
                .nombre(profesional.getIdusuario().getIdauth().getName())
                .apellido(profesional.getIdusuario().getIdauth().getLastname())
                .oficio(profesional.getIdoficio().getOficio())
                .telefono(profesional.getIdusuario().getTelefono())
                .rangoPrecio(rangoPrecio)
                .disponibilidad(diaDisponible)
                .especialidades(especialidadesList)
                .build();
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
}
