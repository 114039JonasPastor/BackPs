package ar.edu.utn.frc.tup.app.dtos.request.registro;

import ar.edu.utn.frc.tup.app.entities.Direccione;
import ar.edu.utn.frc.tup.app.entities.TiposDocumento;
import ar.edu.utn.frc.tup.app.entities.Usuario;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Data
@Getter
@Setter
public class NuevoCliente {
    private Integer idUsuario;
    private Integer idTipoDoc;
    private String documento;
    private String telefono;
    private LocalTime fechaNacimiento;
    private Integer idDireccion;
}
