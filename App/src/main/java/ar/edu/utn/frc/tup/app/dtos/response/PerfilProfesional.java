package ar.edu.utn.frc.tup.app.dtos.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PerfilProfesional {
    private String nombre;
    private String apellido;
    private String oficio;
    private String telefono;
    private String rangoPrecio;
    private String disponibilidad;
}
