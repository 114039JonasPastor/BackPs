package ar.edu.utn.frc.tup.app.dtos.response.trabajo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrabajoClienteResponse {
    private Integer idTrabajo;
    private Integer idSolicitud;
    private String idpago;
    private String estado;
    private String montoFinal;
}
