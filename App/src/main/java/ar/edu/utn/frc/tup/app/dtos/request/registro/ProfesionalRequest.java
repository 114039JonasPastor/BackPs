package ar.edu.utn.frc.tup.app.dtos.request.registro;

import ar.edu.utn.frc.tup.app.entities.Oficio;
import ar.edu.utn.frc.tup.app.entities.Usuario;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ProfesionalRequest {
    private Integer idUsuario;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private Integer idOficio;
}
