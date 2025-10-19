package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.entities.Factura;
import ar.edu.utn.frc.tup.app.services.FacturaService;
import ar.edu.utn.frc.tup.app.services.MercadoPagoMarketPlaceService;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

            Preference preference = mercadoPagoService.crearPreferenciaMarketPlace(factura, profesionalMPUserId);

            Map<String, Object> response = new HashMap<>();
            response.put("preference_id", preference.getId());
            response.put("init_point", preference.getInitPoint());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al crear preferencia: " + e.getMessage()));
        }
    }
}


