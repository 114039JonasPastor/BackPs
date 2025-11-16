package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.dtos.request.factura.FacturaRequest;
import ar.edu.utn.frc.tup.app.dtos.response.PagoFactura;
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
            log.info("Access Token configurado: {}...", accessToken.substring(0, 20));
            log.info("Frontend URL base: {}", frontendUrl);
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
                    .id(String.valueOf(factura.getId()))
                    .title(request.getTitulo())
                    .description(request.getDescripcion())
                    .quantity(request.getCantidad() != null ? request.getCantidad() : 1)
                    .currencyId("ARS")
                    .unitPrice(request.getMonto())
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(item);

            // URLs de retorno - Asegurar que NO tengan doble barra
            String baseUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(baseUrl + "/pago-exitoso")
                    .failure(baseUrl + "/pago-fallido")
                    .pending(baseUrl + "/pago-pendiente")
                    .build();

            log.info("URLs de retorno configuradas:");
            log.info("Success: {}", backUrls.getSuccess());
            log.info("Failure: {}", backUrls.getFailure());
            log.info("Pending: {}", backUrls.getPending());

            // Configurar métodos de pago
            PreferencePaymentMethodsRequest paymentMethods = PreferencePaymentMethodsRequest.builder()
                    .installments(12)
                    .defaultInstallments(1)
                    .build();

            // Crear la preferencia SIN autoReturn primero
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .paymentMethods(paymentMethods)
                    .notificationUrl(webhookUrl)
                    .externalReference(String.valueOf(factura.getId()))
                    .statementDescriptor("Tu Oficio")
                    .build();

            log.info("PreferenceRequest configurado:");
            log.info("Items: {}", items.size());
            log.info("Notification URL: {}", webhookUrl);
            log.info("External Reference: {}", factura.getId());

            // Crear preferencia con el cliente de MercadoPago
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            log.info("Preferencia creada exitosamente");
            log.info("Preference ID: {}", preference.getId());

            boolean isSandbox = accessToken != null && accessToken.startsWith("TEST-");
            String initUrl = isSandbox ? preference.getSandboxInitPoint() : preference.getInitPoint();

            log.info("Modo sandbox: {}", isSandbox);
            log.info("Init URL: {}", initUrl);

            return PreferenceResponse.builder()
                    .preferenceId(preference.getId())
                    .initPoint(initUrl)
                    .sandboxInitPoint(preference.getSandboxInitPoint())
                    .build();

        } catch (MPApiException e) {
            log.error("Error MPApiException");
            log.error("Status Code: {}", e.getStatusCode());
            log.error("Message: {}", e.getMessage());

            if (e.getApiResponse() != null) {
                log.error("API Response Content: {}", e.getApiResponse().getContent());
                log.error("API Response Status Code: {}", e.getApiResponse().getStatusCode());
            }

            throw new RuntimeException("Error MercadoPago API: " + e.getMessage(), e);
        } catch (MPException e) {
            log.error("Error MPException: {}", e.getMessage(), e);
            throw new RuntimeException("Error MercadoPago: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al crear preferencia", e);
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
            log.info("Factura pendiente creada con ID: {}", facturaSaved.getId());

            return facturaSaved;

        } catch (Exception e) {
            log.error("Error al crear factura pendiente", e);
            throw new RuntimeException("Error al crear factura: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Factura procesarPagoAprobado(Map<String, Object> paymentData) {
        try {
            log.info("========== PROCESAR PAGO APROBADO ==========");
            log.info("Payment Data: {}", paymentData);

            // Intentar obtener external_reference de diferentes maneras
            String externalReference = null;

            if (paymentData.get("external_reference") != null) {
                externalReference = paymentData.get("external_reference").toString();
            }

            if (externalReference != null) {
                Integer facturaId = Integer.valueOf(externalReference);
                Factura factura = facturaRepository.findById(facturaId)
                        .orElseThrow(() -> new RuntimeException("Factura no encontrada con ID: " + facturaId));

                factura.setEstadopago("APROBADO");
                Factura facturaSaved = facturaRepository.save(factura);
                log.info("Factura actualizada a APROBADO con ID: {}", facturaSaved.getId());
                return facturaSaved;
            } else {
                // Si no hay external_reference, buscar factura pendiente más reciente
                List<Factura> facturasPendientes = facturaRepository.findByEstadopagoOrderByFechaDesc("PENDIENTE");
                if (!facturasPendientes.isEmpty()) {
                    Factura factura = facturasPendientes.get(0);
                    factura.setEstadopago("APROBADO");
                    Factura facturaSaved = facturaRepository.save(factura);
                    log.info("Factura actualizada a APROBADO (por fecha) con ID: {}", facturaSaved.getId());
                    return facturaSaved;
                }
            }

            throw new RuntimeException("No se pudo procesar el pago - no hay facturas pendientes");

        } catch (Exception e) {
            log.error("Error al procesar pago aprobado", e);
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
            log.info("Estado actualizado correctamente");
        } catch (Exception e) {
            log.error("Error al actualizar estado de factura", e);
            throw new RuntimeException("Error al actualizar estado: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PagoFactura> historialDeIngresos(Instant desde, Instant hasta) {
        List<Factura> facturas = facturaRepository.findByFechaBetweenAndEstadopago(desde, hasta, "APROBADO");

        if(facturas.isEmpty()) {
            throw  new RuntimeException("No existen pagos en este rango de fechas");
        } else{
            List<PagoFactura> pagos = new ArrayList<>();
            for(Factura factura : facturas) {
                PagoFactura pago = PagoFactura.builder()
                        .fecha(factura.getFecha())
                        .monto(factura.getImporte())
                        .medioPago(factura.getIdmediopago().getDescripcion())
                        .build();
                pagos.add(pago);
            }
            return pagos;
        }
    }
}


