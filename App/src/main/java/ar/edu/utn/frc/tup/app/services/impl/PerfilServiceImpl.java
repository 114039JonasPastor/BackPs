package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.dtos.request.perfil.ModificarCliente;
import ar.edu.utn.frc.tup.app.dtos.request.perfil.ModificarProfesional;
import ar.edu.utn.frc.tup.app.dtos.response.PerfilCliente;
import ar.edu.utn.frc.tup.app.dtos.response.PerfilProfesional;
import ar.edu.utn.frc.tup.app.entities.*;
import ar.edu.utn.frc.tup.app.repositories.*;
import ar.edu.utn.frc.tup.app.services.PerfilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PerfilServiceImpl implements PerfilService {

    private final UsuarioRepository usuarioRepository;

    private final AuthRepository authRepository;

    private final DireccionRepository direccionRepository;

    private final ProfesionalRepository professionelleRepository;

    private final MontoRepository montoRepository;

    private final DisponibilidadRepository disponibilidadRepository;

    @Override
    public PerfilCliente getPerfilCliente(Integer idCliente) {
        Usuario usuario = usuarioRepository.findById(idCliente).orElse(null);
        if(usuario != null){
            PerfilCliente perfil = PerfilCliente.builder()
                    .name(usuario.getIdauth().getName())
                    .lastName(usuario.getIdauth().getLastname())
                    .email(usuario.getIdauth().getUsername())
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
        Disponibilidad disponibilidad = disponibilidadRepository.findByIdprofesional(profesional.getId()).orElse(null);
        Monto monto = montoRepository.findByIdprofesional(profesional.getId()).orElse(null);

        String rangoPrecio = monto.getPreciomin().toString() + " - " + monto.getPreciomax().toString();

        //Fixme mejorar el manejo de horario(puede que haya que cambiarlo desde la base de datos)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String diaDisponible = disponibilidad.getDiasemana() + " de " +
                disponibilidad.getHorainicio().format(formatter) + " a " +
                disponibilidad.getHorafin().format(formatter);

        return PerfilProfesional.builder()
                .nombre(profesional.getIdusuario().getIdauth().getName())
                .apellido(profesional.getIdusuario().getIdauth().getLastname())
                .oficio(profesional.getIdoficio().getOficio())
                .telefono(profesional.getIdusuario().getTelefono())
                .rangoPrecio(rangoPrecio)
                .disponibilidad(diaDisponible)
                .build();
    }

    //Todo revisar este metodo
    @Override
    public PerfilProfesional updatePerfilProfesional(ModificarProfesional profesional) {
        Auth auth = authRepository.findByMail(profesional.getMail()).orElse(null);
        Direccione direccion = direccionRepository.findById(profesional.getDireccion().getId()).orElse(null);

        if(auth == null){
            throw new RuntimeException("Usuario no encontrado");
        }

        auth.setName(profesional.getNombre());
        auth.setLastname(profesional.getApellido());
        authRepository.save(auth);

        direccion.setIdbarrio(profesional.getDireccion().getIdbarrio());
        direccion.setCalle(profesional.getDireccion().getCalle());
        direccion.setNumero(profesional.getDireccion().getNumero());
        direccion.setPiso(profesional.getDireccion().getPiso());
        direccion.setDepto(profesional.getDireccion().getDepto());
        direccion.setObservaciones(profesional.getDireccion().getObservaciones());

        direccionRepository.save(direccion);

        Usuario usuario = usuarioRepository.findByIdauth(auth).orElse(null);
        if(usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }
        usuario.setTelefono(profesional.getTelefono());
        usuario.setIddireccion(direccion);

        usuarioRepository.save(usuario);

        Profesionale pro = professionelleRepository.findByIdusuario(usuario.getId()).orElse(null);

        if(pro == null) {
            throw new RuntimeException("Profesional no encontrado");
        }
        pro.setIdoficio(profesional.getOficio());

        professionelleRepository.save(pro);

        return PerfilProfesional.builder()
                .nombre(profesional.getNombre())
                .apellido(profesional.getApellido())
                .oficio(profesional.getOficio().getOficio())
                .telefono(usuario.getTelefono())
                .build();
    }
}
