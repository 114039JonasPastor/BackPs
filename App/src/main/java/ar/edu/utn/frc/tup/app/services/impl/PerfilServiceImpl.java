package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.dtos.DomicilioDto;
import ar.edu.utn.frc.tup.app.dtos.request.perfil.ModificarCliente;
import ar.edu.utn.frc.tup.app.dtos.response.PerfilCliente;
import ar.edu.utn.frc.tup.app.entities.Auth;
import ar.edu.utn.frc.tup.app.entities.Direccione;
import ar.edu.utn.frc.tup.app.entities.Usuario;
import ar.edu.utn.frc.tup.app.repositories.*;
import ar.edu.utn.frc.tup.app.services.PerfilService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PerfilServiceImpl implements PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final AuthRepository authRepository;
    private final DireccionRepository direccionRepository;
    private final CiudadRepository ciudadRepository;
    private final DepartamentoRepository departamentoRepository;
    private final BarrioRepository barrioRepository;

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
        usuario.setIddireccion(usuario.getIddireccion());

        usuarioRepository.save(usuario);

        return PerfilCliente.builder()
                .name(auth.getName())
                .lastName(auth.getLastname())
                .email(auth.getUsername())
                .build();
    }
}
