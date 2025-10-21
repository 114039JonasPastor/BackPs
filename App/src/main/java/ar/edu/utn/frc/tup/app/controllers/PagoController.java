package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.entities.Factura;
import ar.edu.utn.frc.tup.app.services.FacturaService;
import ar.edu.utn.frc.tup.app.services.MercadoPagoMarketPlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PagoController {

    private final MercadoPagoMarketPlaceService mercadoPagoService;

    private final FacturaService facturaService;

    @PostMapping("/crear-preferencia/{facturaId}")
    public ResponseEntity<Map<String, Object>> crearPreferencia(@PathVariable Integer facturaId) {
        try {
            Factura factura = facturaService.findById(facturaId);
            String profesionalMPUserId = "ID_DEL_PROFESIONAL_EN_MP";

            Object preference = mercadoPagoService.crearPreferenciaMarketPlace(factura, profesionalMPUserId);

            Map<String, Object> response = new HashMap<>();

            // Manejar tanto preferencias reales como mock
            if (preference.getClass().getSimpleName().contains("Mock")) {
                // Respuesta para mock
                response.put("preference_id", getPropertyValue(preference, "getId"));
                response.put("init_point", getPropertyValue(preference, "getInitPoint"));
                response.put("mode", "MOCK - Solo para desarrollo");
            } else {
                // Respuesta para MercadoPago real
                response.put("preference_id", getPropertyValue(preference, "getId"));
                response.put("init_point", getPropertyValue(preference, "getInitPoint"));
                response.put("mode", "PRODUCTION");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al crear preferencia: " + e.getMessage()));
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
