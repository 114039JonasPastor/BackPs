package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.dtos.request.FacturaRequest;
import ar.edu.utn.frc.tup.app.entities.Factura;
import ar.edu.utn.frc.tup.app.services.FacturaService;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pagos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PagoController {

    private final FacturaService facturaService;

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @PostMapping("/comprar-directo")
    public ResponseEntity<Map<String, Object>> comprarDirecto(@RequestBody FacturaRequest request) {
        try {
            // Usar el token desde configuración en lugar de hardcodeado
            MercadoPagoConfig.setAccessToken(accessToken);

            // Validar datos de entrada
            if (request.getImporte() == null || request.getImporte().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El importe debe ser mayor a cero"));
            }

            // Validar que el importe no exceda el límite de la base de datos
            BigDecimal limiteMaximo = new BigDecimal("99999999.99");
            if (request.getImporte().compareTo(limiteMaximo) > 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El importe no puede exceder $99,999,999.99"));
            }

            if (request.getProfesionalId() == null || request.getClienteId() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "ProfesionalId y ClienteId son obligatorios"));
            }

            // Generar factura
            Factura facturaGuardada = facturaService.generarFactura(
                    request.getImporte(),
                    request.getProfesionalId(),
                    request.getClienteId()
            );

            // Crear preferencia de MercadoPago
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id("backps_" + facturaGuardada.getId())
                    .title("Servicio Profesional")
                    .description("Pago por servicios profesionales")
                    .quantity(1)
                    .currencyId("ARS")
                    .unitPrice(request.getImporte())
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(itemRequest);

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("facturaId", facturaGuardada.getId());
            response.put("importe", facturaGuardada.getImporte());
            response.put("preference_id", preference.getId());
            response.put("init_point", preference.getInitPoint());
            response.put("sandbox_init_point", preference.getSandboxInitPoint());
            response.put("message", "Compra lista para procesar");

            return ResponseEntity.ok(response);

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "El importe excede el límite permitido. Máximo: $99,999,999.99",
                    "type", "DataIntegrityViolationException"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Error al procesar compra: " + e.getMessage(),
                    "type", e.getClass().getSimpleName()
            ));
        }
    }

    // ... resto de métodos sin cambios
}





