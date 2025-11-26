package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.dtos.common.ErrorApi;
import ar.edu.utn.frc.tup.app.dtos.request.factura.FacturaRequest;
import ar.edu.utn.frc.tup.app.dtos.response.PagoFactura;
import ar.edu.utn.frc.tup.app.dtos.response.PreferenceResponse;
import ar.edu.utn.frc.tup.app.entities.Departamento;
import ar.edu.utn.frc.tup.app.entities.Factura;
import ar.edu.utn.frc.tup.app.services.FacturaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pagos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class PagoController {

    private final FacturaService facturaService;

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @Value("${mercadopago.public.key}")
    private String publicKey;

    // ⭐ Ahora espera idTrabajo en el request
    @PostMapping("/crear-preferencia")
    public ResponseEntity<PreferenceResponse> crearPreferencia(@RequestBody FacturaRequest request) {
        try {
            log.info("📝 Solicitud de creación de preferencia recibida");
            log.info("ID Trabajo: {}", request.getIdTrabajo());
            PreferenceResponse response = facturaService.crearPreferenciaPago(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error al crear preferencia", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            boolean isSandbox = accessToken != null && accessToken.startsWith("TEST-");

            config.put("publicKey", publicKey);
            config.put("sandbox", isSandbox);

            log.info("📤 Configuración MercadoPago enviada - Sandbox: {}", isSandbox);
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("❌ Error obteniendo configuración MercadoPago", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhookMercadoPago(
            @RequestBody Map<String, Object> payload,
            @RequestHeader Map<String, String> headers) {
        try {
            log.info("========== WEBHOOK RECIBIDO ==========");
            log.info("Payload: {}", payload);
            log.info("Headers: {}", headers);

            String type = (String) payload.get("type");
            String action = (String) payload.get("action");

            log.info("Type: {}, Action: {}", type, action);

            if ("payment".equals(type)) {
                Object dataObj = payload.get("data");
                if (dataObj instanceof Map) {
                    Map<String, Object> data = (Map<String, Object>) dataObj;
                    String paymentId = (String) data.get("id");
                    log.info("💰 Payment ID recibido: {}", paymentId);

                    if (action != null && action.startsWith("payment")) {
                        facturaService.procesarPagoAprobado(data);
                        log.info("🔄 Procesando acción: {}", action);
                    }
                }
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("❌ Error procesando webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/historial-ingresos")
    public ResponseEntity<?> historialDeIngresos(
            @RequestParam(required = false) Instant desde,
            @RequestParam(required = false) Instant hasta,
            @RequestParam Integer idProfesional) {
        try {
            List<PagoFactura> pagos = facturaService.historialDeIngresos(desde, hasta, idProfesional);
            return ResponseEntity.ok(pagos);
        } catch (RuntimeException e) {
            ErrorApi error = ErrorApi.builder()
                    .timestamp(Instant.now().toString())
                    .status(HttpStatus.NOT_FOUND.value())
                    .error("Not Found")
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}

