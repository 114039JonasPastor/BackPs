package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.dtos.request.FacturaRequest;
import ar.edu.utn.frc.tup.app.entities.Factura;
import ar.edu.utn.frc.tup.app.services.FacturaService;
import ar.edu.utn.frc.tup.app.services.MercadoPagoMarketPlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PagoController {

    private final MercadoPagoMarketPlaceService mercadoPagoService;
    private final FacturaService facturaService;

    @PostMapping("/generar-factura")
    public ResponseEntity<Map<String, Object>> generarFactura(@RequestBody FacturaRequest request) {
        try {
            Factura facturaGuardada = facturaService.generarFactura(
                    request.getImporte(),
                    request.getProfesionalId(),
                    request.getClienteId()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("facturaId", facturaGuardada.getId());
            response.put("importe", facturaGuardada.getImporte());
            response.put("mensaje", "Factura generada exitosamente");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al generar factura: " + e.getMessage()));
        }
    }

    @PostMapping("/crear-preferencia/{facturaId}")
    public ResponseEntity<Map<String, Object>> crearPreferencia(@PathVariable Integer facturaId) {
        try {
            // Validar que la factura existe y tiene datos completos
            Factura factura = facturaService.findById(facturaId);

            if (factura.getImporte() == null || factura.getImporte().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "La factura no tiene un importe válido"));
            }

            if (factura.getIdprofesional() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "La factura no tiene un profesional asociado"));
            }

            // Usar ID hardcodeado por ahora (será reemplazado con el ID real del profesional)
            String profesionalMPUserId = "ID_DEL_PROFESIONAL_EN_MP";

            Object preference = mercadoPagoService.crearPreferenciaMarketPlace(factura, profesionalMPUserId);

            // Validar que la preferencia se creó correctamente
            if (preference == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No se pudo crear la preferencia de pago"));
            }

            Map<String, Object> response = new HashMap<>();

            // Manejar tanto preferencias reales como mock
            if (preference.getClass().getSimpleName().contains("Mock")) {
                response.put("preference_id", getPropertyValue(preference, "getId"));
                response.put("init_point", getPropertyValue(preference, "getInitPoint"));
                response.put("mode", "MOCK - Solo para desarrollo");
                response.put("message", "Simulación de pago activada");
            } else {
                response.put("preference_id", getPropertyValue(preference, "getId"));
                response.put("init_point", getPropertyValue(preference, "getInitPoint"));
                response.put("mode", "PRODUCTION");
                response.put("message", "Preferencia de pago real creada");
            }

            // Validar que se obtuvieron los valores necesarios
            if (response.get("preference_id") == null || response.get("preference_id").equals("N/A")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "La preferencia se creó pero no se pudo obtener el ID"));
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al crear preferencia: " + e.getMessage()));
        }
    }

    @PostMapping("/webhook/mock/exito/{facturaId}")
    public ResponseEntity<Map<String, String>> simulateSuccessPayment(@PathVariable Integer facturaId) {
        try {
            facturaService.actualizarEstado(facturaId, "PAGADA");

            Map<String, String> response = new HashMap<>();
            response.put("message", "Pago simulado exitosamente - Factura actualizada");
            response.put("facturaId", facturaId.toString());
            response.put("nuevoEstado", "PAGADA");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al simular pago: " + e.getMessage()));
        }
    }

    @PostMapping("/webhook/mock/fallo/{facturaId}")
    public ResponseEntity<Map<String, String>> simulateFailurePayment(@PathVariable Integer facturaId) {
        try {
            facturaService.actualizarEstado(facturaId, "CANCELADA");

            Map<String, String> response = new HashMap<>();
            response.put("message", "Pago fallido simulado - Factura cancelada");
            response.put("facturaId", facturaId.toString());
            response.put("nuevoEstado", "CANCELADA");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al simular pago fallido: " + e.getMessage()));
        }
    }

    private Object getPropertyValue(Object object, String methodName) {
        try {
            return object.getClass().getMethod(methodName).invoke(object);
        } catch (Exception e) {
            return "N/A";
        }
    }
}

