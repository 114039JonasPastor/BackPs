package ar.edu.utn.frc.tup.app.services;

import ar.edu.utn.frc.tup.app.dtos.request.factura.FacturaRequest;
import ar.edu.utn.frc.tup.app.dtos.response.PreferenceResponse;
import ar.edu.utn.frc.tup.app.entities.Factura;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface FacturaService {
    PreferenceResponse crearPreferenciaPago(FacturaRequest request);
    Factura procesarPagoAprobado(Map<String, Object> paymentData);
    Factura obtenerFacturaPorId(Integer nroFactura);
    void actualizarEstadoPago(Integer nroFactura, String estado);
}
