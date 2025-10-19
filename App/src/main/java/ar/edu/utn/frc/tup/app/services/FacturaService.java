package ar.edu.utn.frc.tup.app.services;

import ar.edu.utn.frc.tup.app.entities.Factura;
import org.springframework.stereotype.Service;

@Service
public interface FacturaService {
    Factura findById(Integer id);
}
