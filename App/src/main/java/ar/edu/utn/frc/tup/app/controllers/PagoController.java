package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.dtos.request.FacturaRequest;
import ar.edu.utn.frc.tup.app.entities.Factura;
import ar.edu.utn.frc.tup.app.services.FacturaService;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.resources.preference.Preference;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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

    @Value("${mercadopago.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader Map<String, String> headers,
            HttpServletRequest request) {

        try {
            System.out.println("=== WEBHOOK RECIBIDO ===");
            System.out.println("Headers: " + headers);
            System.out.println("Payload: " + payload);
            System.out.println("Query params: " + request.getQueryString());

            // Procesar la notificación
            facturaService.processPaymentNotification(payload);

            return ResponseEntity.ok("Notificación procesada con éxito");

        } catch (Exception e) {
            System.err.println("❌ Error procesando webhook: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al procesar la notificación");
        }
    }

    @PostMapping("/comprar-testing")
    public ResponseEntity<Map<String, Object>> comprarTesting(@RequestBody FacturaRequest request) {
        try {
            System.out.println("=== COMPRA EN MODO TESTING ===");

            MercadoPagoConfig.setAccessToken(accessToken);

            if (request.getImporte() == null || request.getImporte().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El importe debe ser mayor a cero"));
            }

            Factura facturaGuardada = facturaService.generarFactura(
                    request.getImporte(),
                    request.getProfesionalId(),
                    request.getClienteId()
            );

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title("Servicio Profesional - TuOficio")
                    .description("Pago de servicios profesionales")
                    .quantity(1)
                    .currencyId("ARS")
                    .unitPrice(request.getImporte())
                    .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("https://unrabbeted-chi-clerkly.ngrok-free.dev/api/v1/pagos/success")
                    .failure("https://unrabbeted-chi-clerkly.ngrok-free.dev/api/v1/pagos/failure")
                    .pending("https://unrabbeted-chi-clerkly.ngrok-free.dev/api/v1/pagos/pending")
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(List.of(item))
                    .backUrls(backUrls)
                    // .autoReturn("approved")  // ❌ ELIMINAR
                    .binaryMode(true)
                    .externalReference("FACTURA_" + facturaGuardada.getId())
                    .notificationUrl("https://unrabbeted-chi-clerkly.ngrok-free.dev/api/v1/pagos/webhook")
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            return ResponseEntity.ok(Map.of(
                    "preference_id", preference.getId(),
                    "sandbox_init_point", preference.getSandboxInitPoint(),
                    "facturaId", facturaGuardada.getId(),
                    "testing_instructions", "Usa tarjeta 4509953566233704 - Titular: APRO"
            ));

        } catch (com.mercadopago.exceptions.MPApiException mpApiEx) {
            System.err.println("❌ ERROR MP API: " + mpApiEx.getStatusCode());
            System.err.println("Response: " + mpApiEx.getApiResponse().getContent());

            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Api error. Check response for details",
                    "statusCode", String.valueOf(mpApiEx.getStatusCode()),
                    "apiResponse", mpApiEx.getApiResponse().getContent()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/success")
    public ResponseEntity<String> paymentSuccess(@RequestParam Map<String, String> params) {
        System.out.println("=== PAGO EXITOSO ===");
        System.out.println("Parámetros recibidos: " + params);

        String html = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Pago Exitoso</title>
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                        display: flex; 
                        justify-content: center; 
                        align-items: center; 
                        height: 100vh; 
                        margin: 0; 
                    }
                    .container { 
                        background: white; 
                        padding: 40px; 
                        border-radius: 15px; 
                        box-shadow: 0 10px 30px rgba(0,0,0,0.2); 
                        text-align: center; 
                        max-width: 500px; 
                    }
                    .success-icon { 
                        font-size: 80px; 
                        margin-bottom: 20px; 
                    }
                    h1 { 
                        color: #10b981; 
                        margin: 0 0 10px 0; 
                    }
                    .details { 
                        background: #f9fafb; 
                        padding: 20px; 
                        border-radius: 10px; 
                        margin: 20px 0; 
                        text-align: left; 
                    }
                    .details p { 
                        margin: 10px 0; 
                        color: #4b5563; 
                    }
                    .details strong { 
                        color: #1f2937; 
                    }
                    .btn { 
                        background: #10b981; 
                        color: white; 
                        padding: 15px 40px; 
                        border: none; 
                        border-radius: 8px; 
                        text-decoration: none; 
                        display: inline-block; 
                        margin-top: 20px; 
                        cursor: pointer; 
                        font-size: 16px;
                    }
                    .btn:hover { 
                        background: #059669; 
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="success-icon">✅</div>
                    <h1>¡Pago Exitoso!</h1>
                    <p style="color: #6b7280; margin: 15px 0;">Tu pago se procesó correctamente</p>
                    
                    <div class="details">
                        <p><strong>ID de Pago:</strong> %s</p>
                        <p><strong>Estado:</strong> Aprobado</p>
                        <p><strong>Referencia:</strong> %s</p>
                        <p><strong>Tipo de Pago:</strong> %s</p>
                    </div>
                    
                    <a href="/" class="btn">Volver al inicio</a>
                </div>
            </body>
            </html>
            """.formatted(
                params.getOrDefault("payment_id", "N/A"),
                params.getOrDefault("external_reference", "N/A"),
                params.getOrDefault("payment_type", "N/A")
        );

        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html);
    }

    @GetMapping("/failure")
    public ResponseEntity<String> paymentFailure(@RequestParam Map<String, String> params) {
        System.out.println("=== PAGO FALLIDO ===");
        System.out.println("Parámetros recibidos: " + params);

        String html = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Pago Fallido</title>
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); 
                        display: flex; 
                        justify-content: center; 
                        align-items: center; 
                        height: 100vh; 
                        margin: 0; 
                    }
                    .container { 
                        background: white; 
                        padding: 40px; 
                        border-radius: 15px; 
                        box-shadow: 0 10px 30px rgba(0,0,0,0.2); 
                        text-align: center; 
                        max-width: 500px; 
                    }
                    .error-icon { 
                        font-size: 80px; 
                        margin-bottom: 20px; 
                    }
                    h1 { 
                        color: #ef4444; 
                        margin: 0 0 10px 0; 
                    }
                    .details { 
                        background: #fef2f2; 
                        padding: 20px; 
                        border-radius: 10px; 
                        margin: 20px 0; 
                        text-align: left; 
                    }
                    .details p { 
                        margin: 10px 0; 
                        color: #4b5563; 
                    }
                    .btn { 
                        background: #ef4444; 
                        color: white; 
                        padding: 15px 40px; 
                        border: none; 
                        border-radius: 8px; 
                        text-decoration: none; 
                        display: inline-block; 
                        margin-top: 20px; 
                        cursor: pointer; 
                        font-size: 16px;
                    }
                    .btn:hover { 
                        background: #dc2626; 
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="error-icon">❌</div>
                    <h1>Pago Rechazado</h1>
                    <p style="color: #6b7280; margin: 15px 0;">No se pudo procesar el pago</p>
                    
                    <div class="details">
                        <p><strong>Estado:</strong> Rechazado</p>
                        <p><strong>Motivo:</strong> %s</p>
                        <p><strong>Referencia:</strong> %s</p>
                    </div>
                    
                    <a href="/" class="btn">Intentar nuevamente</a>
                </div>
            </body>
            </html>
            """.formatted(
                params.getOrDefault("status_detail", "No especificado"),
                params.getOrDefault("external_reference", "N/A")
        );

        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html);
    }

    @GetMapping("/pending")
    public ResponseEntity<String> paymentPending(@RequestParam Map<String, String> params) {
        System.out.println("=== PAGO PENDIENTE ===");
        System.out.println("Parámetros recibidos: " + params);

        String html = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Pago Pendiente</title>
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        background: linear-gradient(135deg, #ffecd2 0%%, #fcb69f 100%%); 
                        display: flex; 
                        justify-content: center; 
                        align-items: center; 
                        height: 100vh; 
                        margin: 0; 
                    }
                    .container { 
                        background: white; 
                        padding: 40px; 
                        border-radius: 15px; 
                        box-shadow: 0 10px 30px rgba(0,0,0,0.2); 
                        text-align: center; 
                        max-width: 500px; 
                    }
                    .pending-icon { 
                        font-size: 80px; 
                        margin-bottom: 20px; 
                    }
                    h1 { 
                        color: #f59e0b; 
                        margin: 0 0 10px 0; 
                    }
                    .details { 
                        background: #fffbeb; 
                        padding: 20px; 
                        border-radius: 10px; 
                        margin: 20px 0; 
                        text-align: left; 
                    }
                    .details p { 
                        margin: 10px 0; 
                        color: #4b5563; 
                    }
                    .btn { 
                        background: #f59e0b; 
                        color: white; 
                        padding: 15px 40px; 
                        border: none; 
                        border-radius: 8px; 
                        text-decoration: none; 
                        display: inline-block; 
                        margin-top: 20px; 
                        cursor: pointer; 
                        font-size: 16px;
                    }
                    .btn:hover { 
                        background: #d97706; 
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="pending-icon">⏳</div>
                    <h1>Pago Pendiente</h1>
                    <p style="color: #6b7280; margin: 15px 0;">Tu pago está en proceso de aprobación</p>
                    
                    <div class="details">
                        <p><strong>Estado:</strong> Pendiente</p>
                        <p><strong>ID de Pago:</strong> %s</p>
                        <p><strong>Referencia:</strong> %s</p>
                    </div>
                    
                    <a href="/" class="btn">Volver al inicio</a>
                </div>
            </body>
            </html>
            """.formatted(
                params.getOrDefault("payment_id", "N/A"),
                params.getOrDefault("external_reference", "N/A")
        );

        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html);
    }


    @GetMapping("/datos-testing-completos")
    public ResponseEntity<Map<String, Object>> getDatosTestingCompletos() {
        Map<String, Object> response = new HashMap<>();

        // Usuario comprador (OBLIGATORIO usar para login)
        response.put("usuario_comprador", Map.of(
                "email", "test_user_6675258950908548968@testuser.com",
                "password", "uqvqdJl1dh",
                "user_id", "2964220561"
        ));

        // Tarjetas aprobadas automáticamente
        response.put("tarjetas_aprobadas", Map.of(
                "visa_aprobada", Map.of(
                        "numero", "4509953566233704",
                        "cvv", "123",
                        "vencimiento", "11/25",
                        "titular", "APRO",
                        "dni", "12345678"
                ),
                "mastercard_aprobada", Map.of(
                        "numero", "5031755734530604",
                        "cvv", "123",
                        "vencimiento", "11/25",
                        "titular", "APRO",
                        "dni", "12345678"
                )
        ));

        // Proceso correcto
        response.put("proceso_correcto", List.of(
                "1. Ejecutar: POST /comprar-testing",
                "2. Abrir sandbox_init_point en navegador",
                "3. HACER LOGIN con test_user_6675258950908548968@testuser.com",
                "4. Usar tarjeta 4509953566233704 con titular APRO",
                "5. Completar pago"
        ));

        response.put("nota_importante", "DEBES hacer login con el usuario de prueba antes de usar la tarjeta");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/test-simple-preference")
    public ResponseEntity<Map<String, Object>> testSimplePreference() {
        try {
            System.out.println("=== TEST SIMPLE PREFERENCE ===");
            System.out.println("Access Token: " + (accessToken != null ? accessToken.substring(0, 10) + "..." : "NULL"));

            MercadoPagoConfig.setAccessToken(accessToken);

            // Preferencia mínima para testing
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title("Test Item - BackPs")
                    .quantity(1)
                    .currencyId("ARS")
                    .unitPrice(new BigDecimal("100.00"))
                    .build();

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(List.of(item))
                    .build();

            System.out.println("Enviando preferencia de prueba...");
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(request);

            System.out.println("✅ Preferencia simple creada!");

            return ResponseEntity.ok(Map.of(
                    "preference_id", preference.getId(),
                    "sandbox_init_point", preference.getSandboxInitPoint(),
                    "status", "success",
                    "message", "Conexión con MercadoPago OK"
            ));

        } catch (com.mercadopago.exceptions.MPApiException mpApiEx) {
            System.err.println("❌ ERROR MP API:");
            System.err.println("Status Code: " + mpApiEx.getStatusCode());
            System.err.println("Response: " + mpApiEx.getApiResponse());

            return ResponseEntity.badRequest().body(Map.of(
                    "error", mpApiEx.getMessage(),
                    "type", "MPApiException",
                    "statusCode", String.valueOf(mpApiEx.getStatusCode()), // Cambio aquí
                    "apiResponse", mpApiEx.getApiResponse() != null ? mpApiEx.getApiResponse() : "Sin respuesta"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "type", e.getClass().getSimpleName()
            ));
        }
    }

    @PostMapping("/test-token")
    public ResponseEntity<Map<String, Object>> testToken() {
        try {
            System.out.println("=== VERIFICANDO TOKEN ===");
            System.out.println("Token configurado: " + accessToken.substring(0, 20) + "...");

            MercadoPagoConfig.setAccessToken(accessToken);

            String tokenActual = MercadoPagoConfig.getAccessToken();
            System.out.println("Token en SDK: " + (tokenActual != null ? tokenActual.substring(0, 20) + "..." : "NULL"));

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title("Test Token - BackPs")
                    .quantity(1)
                    .currencyId("ARS")
                    .unitPrice(new BigDecimal("10.00"))
                    .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("https://unrabbeted-chi-clerkly.ngrok-free.dev/api/v1/pagos/success")
                    .failure("https://unrabbeted-chi-clerkly.ngrok-free.dev/api/v1/pagos/failure")
                    .pending("https://unrabbeted-chi-clerkly.ngrok-free.dev/api/v1/pagos/pending")
                    .build();

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(List.of(item))
                    .backUrls(backUrls)
                    // .autoReturn("approved")  // ❌ ELIMINAR - No funciona con ngrok
                    .externalReference("TEST_TOKEN_" + System.currentTimeMillis())
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(request);

            return ResponseEntity.ok(Map.of(
                    "status", "TOKEN VÁLIDO ✅",
                    "preference_id", preference.getId(),
                    "sandbox_init_point", preference.getSandboxInitPoint(),
                    "token_configured", accessToken.substring(0, 20) + "...",
                    "token_in_sdk", tokenActual.substring(0, 20) + "..."
            ));

        } catch (com.mercadopago.exceptions.MPApiException mpApiEx) {
            System.err.println("❌ ERROR MP API:");
            System.err.println("Status: " + mpApiEx.getStatusCode());
            System.err.println("Response: " + mpApiEx.getApiResponse().getContent());

            return ResponseEntity.badRequest().body(Map.of(
                    "status", "TOKEN INVÁLIDO ❌",
                    "statusCode", String.valueOf(mpApiEx.getStatusCode()),
                    "error", mpApiEx.getMessage(),
                    "apiResponse", mpApiEx.getApiResponse().getContent()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/debug-config")
    public ResponseEntity<Map<String, Object>> debugConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("accessToken_configured", accessToken != null && !accessToken.isEmpty());
        config.put("accessToken_prefix", accessToken != null ? accessToken.substring(0, Math.min(15, accessToken.length())) + "..." : "NULL");
        config.put("webhookSecret_configured", webhookSecret != null && !webhookSecret.isEmpty());
        return ResponseEntity.ok(config);
    }

    @PostMapping("/test-payment")
    public ResponseEntity<Map<String, Object>> testPayment(@RequestBody Map<String, Object> testData) {
        Map<String, Object> response = new HashMap<>();

        // Datos de prueba actualizados
        response.put("seller_test_user", Map.of(
                "user_id", "2935832272",
                "username", "TESTUSER8832380519814420634",
                "password", "tfVrYrjajF",
                "role", "seller"
        ));

        response.put("buyer_test_user", Map.of(
                "user_id", "2964220561",
                "username", "TESTUSER6675258950908548968",
                "password", "uqvqdJl1dh",
                "role", "buyer"
        ));

        response.put("test_cards", Map.of(
                "visa", "4509953566233704",
                "mastercard", "5031755734530604",
                "amex", "373987625633225"
        ));

        response.put("instructions", "Usa estos datos en el sandbox de MercadoPago para simular pagos");
        response.put("email", "tuoficiopracticasupervisada@gmail.com");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-card-data")
    public ResponseEntity<Map<String, Object>> getTestCardData() {
        Map<String, Object> response = new HashMap<>();

        // Tarjetas que NO requieren códigos de validación
        Map<String, Object> approvedCards = new HashMap<>();
        approvedCards.put("visa_approved", Map.of(
                "number", "4509953566233704",
                "cvv", "123",
                "expiry_month", "11",
                "expiry_year", "25",
                "cardholder_name", "APRO",
                "status", "approved_without_validation"
        ));

        approvedCards.put("mastercard_approved", Map.of(
                "number", "5031755734530604",
                "cvv", "123",
                "expiry_month", "11",
                "expiry_year", "25",
                "cardholder_name", "APRO",
                "status", "approved_without_validation"
        ));

        // Tarjetas para probar rechazos
        Map<String, Object> rejectedCards = new HashMap<>();
        rejectedCards.put("visa_rejected", Map.of(
                "number", "4013540682746260",
                "cvv", "123",
                "expiry_month", "11",
                "expiry_year", "25",
                "cardholder_name", "OTHE",
                "status", "rejected"
        ));

        response.put("approved_cards", approvedCards);
        response.put("rejected_cards", rejectedCards);
        response.put("instructions", "Usa estas tarjetas en el checkout de MercadoPago");
        response.put("note", "Las tarjetas con APRO se aprueban automáticamente sin códigos");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-users-correct")
    public ResponseEntity<Map<String, Object>> getCorrectTestUsers() {
        Map<String, Object> response = new HashMap<>();

        // Usuario vendedor (tu marketplace)
        response.put("seller", Map.of(
                "user_id", "2935832272",
                "email", "test_user_8832380519814420634@testuser.com",
                "username", "TESTUSER8832380519814420634",
                "password", "tfVrYrjajF"
        ));

        // Para compras, NO uses el usuario comprador, usa directamente las tarjetas
        response.put("buyer_instructions", Map.of(
                "method", "direct_card_payment",
                "note", "NO inicies sesión como usuario comprador",
                "process", "Usa las tarjetas de prueba directamente en el checkout"
        ));

        response.put("workflow", List.of(
                "1. Crear preferencia de pago (ya funciona)",
                "2. Abrir sandbox_init_point",
                "3. Pagar como invitado (sin login)",
                "4. Usar tarjeta 4509953566233704 con nombre APRO"
        ));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/comprar-sin-usuarios")
    public ResponseEntity<Map<String, Object>> comprarSinUsuarios(@RequestBody FacturaRequest request) {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);

            // Generar factura
            Factura facturaGuardada = facturaService.generarFactura(
                    request.getImporte(),
                    request.getProfesionalId(),
                    request.getClienteId()
            );

            // Crear preferencia optimizada para guest checkout
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title("Servicio Profesional - BackPs")
                    .quantity(1)
                    .currencyId("ARS")
                    .unitPrice(request.getImporte())
                    .build();

            // URLs de retorno
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("https://tu-dominio.com/pago/exitoso")
                    .failure("https://tu-dominio.com/pago/fallido")
                    .pending("https://tu-dominio.com/pago/pendiente")
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(List.of(item))
                    .backUrls(backUrls)
                    .autoReturn("approved")
                    .externalReference(facturaGuardada.getId().toString())
                    .notificationUrl("https://unrabbeted-chi-clerkly.ngrok-free.dev/api/v1/pagos/webhook")
                    // Configurar para checkout de invitado
                    .binaryMode(true)
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("preference_id", preference.getId());
            response.put("sandbox_init_point", preference.getSandboxInitPoint());
            response.put("facturaId", facturaGuardada.getId());
            response.put("message", "✅ Usa como INVITADO con tarjeta 4509953566233704");
            response.put("card_instructions", Map.of(
                    "number", "4509953566233704",
                    "cvv", "123",
                    "expiry", "11/25",
                    "name", "APRO"
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}





