package ar.edu.utn.frc.tup.app.dtos.request.perfil;

import ar.edu.utn.frc.tup.app.entities.Direccione;
import ar.edu.utn.frc.tup.app.entities.Oficio;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ModificarProfesional {
    private String mail;
    private String nombre;
    private String apellido;
    private String telefono;
    private Direccione direccion;
    private Oficio oficio;
}
