package ar.edu.utn.frc.tup.app.dtos.request;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturaRequest {
    private Integer idSolicitud;
    private String titulo;
    private String descripcion;
    private BigDecimal monto;
    private Integer cantidad;
}