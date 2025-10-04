package ar.edu.utn.frc.tup.app.dtos.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PerfilCliente {
    private String name;
    private String lastName;
    private String email;
}
