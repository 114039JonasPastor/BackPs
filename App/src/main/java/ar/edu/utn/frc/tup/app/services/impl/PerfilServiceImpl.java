package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.dtos.response.PerfilCliente;
import ar.edu.utn.frc.tup.app.entities.Usuario;
import ar.edu.utn.frc.tup.app.repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.app.services.PerfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PerfilServiceImpl implements PerfilService {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Override
    public PerfilCliente getPerfilCliente(Integer idCliente) {
        Usuario usuario = usuarioRepository.findById(idCliente).orElse(null);
        if(usuario != null){
            PerfilCliente perfil = new PerfilCliente();
            perfil.setName(usuario.getIdauth().getName());
            perfil.setLastName(usuario.getIdauth().getLastname());
            perfil.setEmail(usuario.getIdauth().getUsername());
            return perfil;
        } else {
            throw new RuntimeException("Usuario no encontrado");
        }
    }
}
