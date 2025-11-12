package ar.edu.utn.frc.tup.app.dtos.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PerfilProfesional {
    private Integer idProfesional;
    private String nombre;
    private String apellido;
    private String oficio;
    private String telefono;
    private String rangoPrecio;
    private String disponibilidad;
    private List<String> especialidades;
}
