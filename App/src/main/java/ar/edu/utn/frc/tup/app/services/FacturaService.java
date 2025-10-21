package ar.edu.utn.frc.tup.app.services;

import ar.edu.utn.frc.tup.app.entities.Factura;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public interface FacturaService {
    Factura findById(Integer id);
    Factura generarFactura(BigDecimal importe, Integer profesionalId, Integer clienteId);
}
