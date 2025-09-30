package ar.edu.utn.frc.tup.app.dtos.request.registro;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UsuarioRequest {
    private String password;
    private String name;
    private String lastName;
    private String email;
    private String documento;
    private String telefono;
    private LocalDate nacimiento;
    private Integer idDireccion;
    private Integer idTipoDoc;
}
