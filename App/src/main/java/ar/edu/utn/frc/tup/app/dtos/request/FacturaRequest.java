package ar.edu.utn.frc.tup.app.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacturaRequest {
    private BigDecimal importe;
    private String descripcion;
    private Integer profesionalId;
    private Integer clienteId;
}
