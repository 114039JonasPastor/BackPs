package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.dtos.request.FacturaRequest;
import ar.edu.utn.frc.tup.app.dtos.response.PreferenceResponse;
import ar.edu.utn.frc.tup.app.entities.Factura;
import ar.edu.utn.frc.tup.app.entities.Mediosdepago;
import ar.edu.utn.frc.tup.app.repositories.FacturaRepository;
import ar.edu.utn.frc.tup.app.repositories.MediosdepagoRepository;
import ar.edu.utn.frc.tup.app.services.FacturaService;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository facturaRepository;
    private final MediosdepagoRepository mediosdepagoRepository;

    @Value("${mercadopago.webhook.url}")
    private String webhookUrl;

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @Value("${mercadopago.frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public PreferenceResponse crearPreferenciaPago(FacturaRequest request) {
        try {
            // Configurar el token de acceso
            MercadoPagoConfig.setAccessToken(accessToken);

            log.info("========== CREAR PREFERENCIA ==========");
            log.info("ID Solicitud: {}", request.getIdSolicitud());
            log.info("Título: {}", request.getTitulo());
            log.info("Monto: {}", request.getMonto());

            // Crear item
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .id(String.valueOf(request.getIdSolicitud()))
                    .title(request.getTitulo())
                    .description(request.getDescripcion())
                    .quantity(request.getCantidad() != null ? request.getCantidad() : 1)
                    .currencyId("ARS")
                    .unitPrice(request.getMonto())
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(item);

            // URLs de retorno
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(frontendUrl + "/pago-exitoso")
                    .failure(frontendUrl + "/pago-fallido")
                    .pending(frontendUrl + "/pago-pendiente")
                    .build();

            // Configurar métodos de pago
            PreferencePaymentMethodsRequest paymentMethods = PreferencePaymentMethodsRequest.builder()
                    .installments(12) // Permitir cuotas
                    .defaultInstallments(1)
                    .build();

            // Crear la preferencia
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .paymentMethods(paymentMethods)
                    .notificationUrl(webhookUrl)
                    .externalReference(String.valueOf(request.getIdSolicitud()))
                    .statementDescriptor("Tu Oficio - Servicio")
                    .autoReturn("approved")
                    .binaryMode(true) // Solo aprobado o rechazado
                    .build();

            // Crear preferencia con el cliente de MercadoPago
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            log.info("✅ Preferencia creada exitosamente");
            log.info("Preference ID: {}", preference.getId());

            // Determinar si estamos en modo sandbox
            boolean isSandbox = accessToken != null && accessToken.startsWith("TEST-");
            String initUrl = isSandbox ? preference.getSandboxInitPoint() : preference.getInitPoint();

            log.info("Modo sandbox: {}", isSandbox);
            log.info("Init URL: {}", initUrl);

            return PreferenceResponse.builder()
                    .preferenceId(preference.getId())
                    .initPoint(initUrl)
                    .sandboxInitPoint(preference.getSandboxInitPoint())
                    .build();

        } catch (MPException | MPApiException e) {
            log.error("❌ Error al crear preferencia de MercadoPago", e);
            throw new RuntimeException("Error MercadoPago: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Error inesperado al crear preferencia", e);
            throw new RuntimeException("Error interno: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Factura procesarPagoAprobado(Map<String, Object> paymentData) {
        try {
            log.info("========== PROCESAR PAGO APROBADO ==========");
            log.info("Payment Data: {}", paymentData);

            Factura factura = new Factura();

            Mediosdepago medioPago = mediosdepagoRepository.findById(1)
                    .orElseThrow(() -> new RuntimeException("Medio de pago no encontrado"));

            factura.setIdmediopago(medioPago);
            factura.setEstadopago("APROBADO");
            factura.setImporte(new BigDecimal(paymentData.get("transaction_amount").toString()));

            Factura facturaSaved = facturaRepository.save(factura);
            log.info("✅ Factura guardada con ID: {}", facturaSaved.getId());

            return facturaSaved;

        } catch (Exception e) {
            log.error("❌ Error al procesar pago aprobado", e);
            throw new RuntimeException("Error al guardar factura: " + e.getMessage(), e);
        }
    }

    @Override
    public Factura obtenerFacturaPorId(Integer nroFactura) {
        log.info("Buscando factura con ID: {}", nroFactura);
        return facturaRepository.findById(nroFactura)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada con ID: " + nroFactura));
    }

    @Override
    @Transactional
    public void actualizarEstadoPago(Integer nroFactura, String estado) {
        try {
            log.info("Actualizando estado de factura {} a {}", nroFactura, estado);
            Factura factura = obtenerFacturaPorId(nroFactura);
            factura.setEstadopago(estado);
            facturaRepository.save(factura);
            log.info("✅ Estado actualizado correctamente");
        } catch (Exception e) {
            log.error("❌ Error al actualizar estado de factura", e);
            throw new RuntimeException("Error al actualizar estado: " + e.getMessage(), e);
        }
    }
}

