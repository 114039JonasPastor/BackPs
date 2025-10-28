package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.entities.Factura;
import ar.edu.utn.frc.tup.app.entities.Mediosdepago;
import ar.edu.utn.frc.tup.app.entities.Profesionale;
import ar.edu.utn.frc.tup.app.entities.Usuario;
import ar.edu.utn.frc.tup.app.repositories.FacturaRepository;
import ar.edu.utn.frc.tup.app.repositories.MediosdepagoRepository;
import ar.edu.utn.frc.tup.app.repositories.ProfesionalRepository;
import ar.edu.utn.frc.tup.app.repositories.UsuarioRepository;
import ar.edu.utn.frc.tup.app.services.FacturaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository facturaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProfesionalRepository profesionaleRepository;
    private final MediosdepagoRepository mediosdePagoRepository;

    public Factura findById(Integer id) {
        return facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada con ID: " + id));
    }

    @Override
    @Transactional
    public Factura generarFactura(BigDecimal importe, Integer profesionalId, Integer clienteId) {
        try {
            Usuario usuario = usuarioRepository.findById(clienteId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Profesionale profesional = profesionaleRepository.findById(profesionalId)
                    .orElseThrow(() -> new RuntimeException("Profesional no encontrado"));

            // Buscar el primer medio de pago disponible
            Mediosdepago medioPago = mediosdePagoRepository.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No hay medios de pago disponibles"));

            Factura factura = new Factura();
            factura.setImporte(importe); // Solo si ya creaste la columna en BD
            factura.setIdusuario(usuario);
            factura.setIdprofesional(profesional);
            factura.setIdmediopago(medioPago);
            factura.setFecha(Instant.now());
            factura.setEstadopago("PENDIENTE");

            return facturaRepository.save(factura);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar factura: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Factura actualizarEstado(Integer facturaId, String nuevoEstado) {
        Factura factura = findById(facturaId);
        factura.setEstadopago(nuevoEstado);
        return facturaRepository.save(factura);
    }
}
