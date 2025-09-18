package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.dtos.UsuarioDto;
import ar.edu.utn.frc.tup.app.entities.Usuario;
import ar.edu.utn.frc.tup.app.repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.app.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService{

    @Autowired
    UsuarioRepository usuarioRepository;

    public List<UsuarioDto> getUsuarios() {
        return usuarioRepository.findAll().stream().map(usuario -> {
            UsuarioDto dto = new UsuarioDto();
            dto.setId(usuario.getId());
            dto.setUsername(usuario.getUsername());
            dto.setMail(usuario.getMail());
            dto.setRoleDescripcion(usuario.getIdrol().getDescripcion());
            return dto;
        }).collect(Collectors.toList());
    }
}
