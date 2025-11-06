package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.dtos.request.FacturaRequest;
import ar.edu.utn.frc.tup.app.dtos.response.PreferenceResponse;
import ar.edu.utn.frc.tup.app.entities.Factura;
import ar.edu.utn.frc.tup.app.entities.Mediosdepago;
import ar.edu.utn.frc.tup.app.entities.Solicitude;
import ar.edu.utn.frc.tup.app.repositories.FacturaRepository;
import ar.edu.utn.frc.tup.app.repositories.MediosdepagoRepository;
import ar.edu.utn.frc.tup.app.repositories.SolicitudeRepository;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository facturaRepository;
    private final MediosdepagoRepository mediosdepagoRepository;
    private final SolicitudeRepository solicitudeRepository;

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

            // Obtener la solicitud
            Solicitude solicitud = solicitudeRepository.findById(request.getIdSolicitud())
                    .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

            // Crear factura pendiente
            Factura factura = crearFacturaPendiente(solicitud, request.getMonto());

            log.info("Factura creada con ID: {}", factura.getId());
            log.info("Título: {}", request.getTitulo());
            log.info("Monto: {}", request.getMonto());

            // Crear item
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .id(String.valueOf(factura.getId())) // Usar ID de factura
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
                    .installments(12)
                    .defaultInstallments(1)
                    .build();

            // Crear la preferencia
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .paymentMethods(paymentMethods)
                    .notificationUrl(webhookUrl)
                    .externalReference(String.valueOf(factura.getId())) // Usar ID de factura
                    .statementDescriptor("Tu Oficio - Servicio")
                    .autoReturn("approved")
                    .binaryMode(true)
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

    @Transactional
    protected Factura crearFacturaPendiente(Solicitude solicitud, BigDecimal monto) {
        try {
            log.info("Creando factura pendiente para solicitud: {}", solicitud.getId());

            // Obtener medio de pago de MercadoPago (asumiendo ID 1)
            Mediosdepago medioPago = mediosdepagoRepository.findById(1)
                    .orElseThrow(() -> new RuntimeException("Medio de pago no encontrado"));

            Factura factura = new Factura();
            factura.setIdusuario(solicitud.getIdusuario());
            factura.setIdprofesional(solicitud.getIdprofesional());
            factura.setIdmediopago(medioPago);
            factura.setImporte(monto);
            factura.setEstadopago("PENDIENTE");
            factura.setFecha(Instant.now());

            Factura facturaSaved = facturaRepository.save(factura);
            log.info("✅ Factura pendiente creada con ID: {}", facturaSaved.getId());

            return facturaSaved;

        } catch (Exception e) {
            log.error("❌ Error al crear factura pendiente", e);
            throw new RuntimeException("Error al crear factura: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Factura procesarPagoAprobado(Map<String, Object> paymentData) {
        try {
            log.info("========== PROCESAR PAGO APROBADO ==========");
            log.info("Payment Data: {}", paymentData);

            // Obtener external_reference que debería ser el ID de la factura
            String externalReference = paymentData.get("external_reference").toString();
            Integer facturaId = Integer.valueOf(externalReference);

            Factura factura = facturaRepository.findById(facturaId)
                    .orElseThrow(() -> new RuntimeException("Factura no encontrada con ID: " + facturaId));

            // Actualizar estado a aprobado
            factura.setEstadopago("APROBADO");

            Factura facturaSaved = facturaRepository.save(factura);
            log.info("✅ Factura actualizada a APROBADO con ID: {}", facturaSaved.getId());

            return facturaSaved;

        } catch (Exception e) {
            log.error("❌ Error al procesar pago aprobado", e);
            throw new RuntimeException("Error al actualizar factura: " + e.getMessage(), e);
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


