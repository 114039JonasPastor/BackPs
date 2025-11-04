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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository facturaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProfesionalRepository profesionaleRepository;
    private final MediosdepagoRepository mediosdePagoRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mercadopago.access.token}")
    private String accessToken;

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

    @Override
    public void processPaymentNotification(String payload) {
        try {
            log.info("Recibida notificación de pago: {}", payload);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(payload);

            // Determinar el tipo de notificación
            if (jsonNode.has("resource")) {
                String resourceUrl = jsonNode.get("resource").asText();
                String topic = jsonNode.has("topic") ? jsonNode.get("topic").asText() : "";

                log.info("Tipo de notificación: {}, URL del recurso: {}", topic, resourceUrl);

                if ("payment".equals(topic) || resourceUrl.contains("payments")) {
                    processPaymentResource(resourceUrl);
                } else if ("merchant_order".equals(topic) || resourceUrl.contains("merchant_orders")) {
                    processMerchantOrderResource(resourceUrl);
                } else {
                    log.warn("Tipo de notificación desconocido: {}", topic);
                }
            } else if (jsonNode.has("data") && jsonNode.get("data").has("id")) {
                Long paymentId = jsonNode.get("data").get("id").asLong();
                boolean isProduction = jsonNode.has("live_mode") ? jsonNode.get("live_mode").asBoolean() : false;

                log.info("Notificación directa de pago ID: {}, Producción: {}", paymentId, isProduction);
                processDirectPaymentNotification(paymentId, isProduction);
            } else {
                log.warn("Formato de notificación no reconocido: {}", payload);
            }

        } catch (Exception e) {
            log.error("Error al procesar notificación de pago", e);
            throw new RuntimeException("Error al procesar notificación de pago: " + e.getMessage(), e);
        }
    }

    private void processPaymentResource(String resourceUrl) {
        try {
            String accessTokenToUse = accessToken;

            if (!resourceUrl.contains("access_token")) {
                resourceUrl += (resourceUrl.contains("?") ? "&" : "?") + "access_token=" + accessTokenToUse;
            }

            log.info("Consultando recurso de pago: {}", resourceUrl);

            ResponseEntity<String> response = restTemplate.getForEntity(resourceUrl, String.class);
            String responseBody = response.getBody();

            log.info("Respuesta del recurso de pago: {}", responseBody);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode paymentData = objectMapper.readTree(responseBody);

            String status = paymentData.has("status") ? paymentData.get("status").asText() : "";
            String externalReference = paymentData.has("external_reference") ?
                    paymentData.get("external_reference").asText() : "";

            log.info("Estado del pago: {}, Referencia externa: {}", status, externalReference);

            updatePaymentStatus(externalReference, status);

        } catch (Exception e) {
            log.error("Error al procesar recurso de pago", e);
            throw new RuntimeException("Error al procesar recurso de pago: " + e.getMessage(), e);
        }
    }

    private void processMerchantOrderResource(String resourceUrl) {
        try {
            String accessTokenToUse = accessToken;

            if (!resourceUrl.contains("access_token")) {
                resourceUrl += (resourceUrl.contains("?") ? "&" : "?") + "access_token=" + accessTokenToUse;
            }

            log.info("Consultando recurso de orden de comerciante: {}", resourceUrl);

            ResponseEntity<String> response = restTemplate.getForEntity(resourceUrl, String.class);
            String responseBody = response.getBody();

            log.info("Respuesta del recurso de orden: {}", responseBody);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode orderData = objectMapper.readTree(responseBody);

            String externalReference = orderData.has("external_reference") ?
                    orderData.get("external_reference").asText() : "";

            JsonNode payments = orderData.has("payments") ? orderData.get("payments") : null;

            if (payments != null && payments.isArray() && payments.size() > 0) {
                boolean allApproved = true;

                for (JsonNode payment : payments) {
                    String status = payment.has("status") ? payment.get("status").asText() : "";

                    log.info("Pago en orden - ID: {}, Estado: {}",
                            payment.has("id") ? payment.get("id").asText() : "", status);

                    if (!"approved".equalsIgnoreCase(status)) {
                        allApproved = false;
                    }
                }

                if (allApproved && payments.size() > 0) {
                    log.info("Todos los pagos aprobados para la orden con referencia: {}", externalReference);
                    updatePaymentStatus(externalReference, "approved");
                }
            }

        } catch (Exception e) {
            log.error("Error al procesar recurso de orden de comerciante", e);
            throw new RuntimeException("Error al procesar recurso de orden de comerciante: " + e.getMessage(), e);
        }
    }

    private void processDirectPaymentNotification(Long paymentId, boolean isProduction) {
        try {
            // Si es ambiente de prueba (sandbox), simular aprobación
            if (paymentId < 1000000 || !isProduction) {
                log.info("Sandbox Payment Detected: Simulando pago aprobado para ID: {}", paymentId);
                return;
            }

            // Consultar el pago a MercadoPago usando la API oficial
            PaymentClient paymentClient = new PaymentClient();
            Payment payment = paymentClient.get(paymentId);

            String status = payment.getStatus();
            String externalReference = payment.getExternalReference();

            log.info("Estado del pago directo: {}, referencia externa: {}", status, externalReference);

            updatePaymentStatus(externalReference, status);

        } catch (Exception e) {
            log.error("Error al procesar notificación directa de pago", e);
            throw new RuntimeException("Error al procesar notificación directa de pago: " + e.getMessage(), e);
        }
    }

    private void updatePaymentStatus(String externalReference, String status) {
        log.info("Actualizando estado de pago. Ref: {}, Estado: {}", externalReference, status);

        if ("approved".equalsIgnoreCase(status)) {
            try {
                // Extraer ID de factura desde external_reference (formato: "FACTURA_123")
                String facturaIdStr = externalReference.replace("FACTURA_", "");
                Integer facturaId = Integer.parseInt(facturaIdStr);

                Factura factura = findById(facturaId);

                if (factura != null) {
                    factura.setEstadopago("APROBADO");
                    facturaRepository.save(factura);

                    log.info("✅ Pago aprobado y guardado correctamente para factura ID: {}", facturaId);
                } else {
                    log.warn("No se encontró factura con ID: {}", facturaId);
                }

            } catch (NumberFormatException e) {
                log.error("Error al convertir referencia externa a ID de factura: {}", externalReference, e);
            } catch (Exception e) {
                log.error("Error al actualizar estado de factura", e);
            }
        } else {
            log.info("Pago no aprobado. Estado: {}", status);
        }
    }
}
