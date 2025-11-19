package ar.edu.utn.frc.tup.app.dtos.response.perfil;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DisponibilidadDto {
    private String diaSemana;
    private String horaInicio;
    private String horaFin;
}

