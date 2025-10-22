package ar.edu.utn.frc.tup.app.services;

import ar.edu.utn.frc.tup.app.dtos.request.perfil.ModificarCliente;
import ar.edu.utn.frc.tup.app.dtos.request.perfil.ModificarProfesional;
import ar.edu.utn.frc.tup.app.dtos.response.PerfilCliente;
import ar.edu.utn.frc.tup.app.dtos.response.PerfilProfesional;
import org.springframework.stereotype.Service;

@Service
public interface PerfilService {
    PerfilCliente getPerfilCliente(Integer idCliente);
    PerfilCliente updatePerfilCliente(ModificarCliente cliente);
    PerfilProfesional getPerfilProfesional(Integer idProfesional);
    PerfilProfesional updatePerfilProfesional(ModificarProfesional profesional);
    void updateAvatar(Integer idAuth, String avatarUrl);
    String getAvatar(Integer idAuth);
}
