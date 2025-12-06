package ar.edu.utn.frc.tup.app.services;

import ar.edu.utn.frc.tup.app.dtos.request.perfil.ModificarCliente;
import ar.edu.utn.frc.tup.app.dtos.request.perfil.ModificarProfesional;
import ar.edu.utn.frc.tup.app.dtos.response.UsuariosRegistradosDto;
import ar.edu.utn.frc.tup.app.dtos.response.perfil.PerfilCliente;
import ar.edu.utn.frc.tup.app.dtos.response.perfil.PerfilProfesional;
import ar.edu.utn.frc.tup.app.dtos.response.perfil.metrica.ProfesionalMetrica;
import ar.edu.utn.frc.tup.app.dtos.response.perfil.metrica.UsuarioMetrica;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PerfilService {
    PerfilCliente getPerfilCliente(Integer idCliente);
    PerfilCliente updatePerfilCliente(ModificarCliente cliente);
    PerfilProfesional getPerfilProfesional(Integer idProfesional);
    PerfilProfesional updatePerfilProfesional(ModificarProfesional profesional);
    void updateAvatar(Integer idAuth, String avatarUrl);
    String getAvatar(Integer idAuth);
    List<PerfilProfesional> getProfesionalesByOficio(String oficio);
    void agregarStrike(Integer idUsuario, String motivo);
    List<UsuarioMetrica> getUsuariosMetrica(Integer limit);
    List<ProfesionalMetrica> getProfesionalesMetrica(Integer limit);
    UsuariosRegistradosDto getUsuariosRegistrados();
}
