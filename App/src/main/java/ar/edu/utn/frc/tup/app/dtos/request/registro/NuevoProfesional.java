package ar.edu.utn.frc.tup.app.dtos.request.registro;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Data
@Getter
@Setter
public class NuevoProfesional {
    private Integer idUsuario;
    private Integer idTipoDoc;
    private Integer idoficio;
    private String documento;
    private String telefono;
    private LocalTime fechaNacimiento;
    private LocalTime fechaDesde;
    private LocalTime fechaHasta;
    private Integer idDireccion;
}
