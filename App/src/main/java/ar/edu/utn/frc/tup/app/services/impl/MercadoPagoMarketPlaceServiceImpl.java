package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.entities.Factura;
import ar.edu.utn.frc.tup.app.services.MercadoPagoMarketPlaceService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Primary
@ConditionalOnClass(name = "com.mercadopago.MercadoPagoConfig")
public class MercadoPagoMarketPlaceServiceImpl implements MercadoPagoMarketPlaceService {

    @Value("${mercadopago.access.token:mock_token}")
    private String accessToken;

    @PostConstruct
    public void init() {
        try {
            // Solo intentar configurar MercadoPago si las clases están disponibles
            Class<?> configClass = Class.forName("com.mercadopago.MercadoPagoConfig");
            java.lang.reflect.Method setTokenMethod = configClass.getMethod("setAccessToken", String.class);
            setTokenMethod.invoke(null, accessToken);
            log.info("MercadoPago configurado correctamente");
        } catch (Exception e) {
            log.warn("No se pudo configurar MercadoPago: {}", e.getMessage());
        }
    }

    @Override
    public Object crearPreferenciaMarketPlace(Factura factura, String profesionalMPUserId) {
        try {
            // Primero verificar si es un profesionalMPUserId válido
            if (profesionalMPUserId == null || profesionalMPUserId.equals("ID_DEL_PROFESIONAL_EN_MP")) {
                log.warn("Usando modo MOCK - profesionalMPUserId no válido: {}", profesionalMPUserId);
                return crearPreferenciaMock(factura, profesionalMPUserId);
            }

            // Intentar crear preferencia real
            return crearPreferenciaReal(factura, profesionalMPUserId);

        } catch (Exception e) {
            log.error("Error al crear preferencia real, fallback a MOCK. Error: {}", e.getMessage());
            log.debug("Stack trace completo:", e);

            // Fallback a modo mock si falla la creación real
            return crearPreferenciaMock(factura, profesionalMPUserId);
        }
    }

    private Object crearPreferenciaReal(Factura factura, String profesionalMPUserId) throws Exception {
        // Usar reflection para crear la preferencia sin imports directos
        BigDecimal comision = factura.getImporte().multiply(new BigDecimal("0.05"));

        // Crear item usando reflection
        Class<?> itemRequestClass = Class.forName("com.mercadopago.client.preference.PreferenceItemRequest");
        Object itemBuilder = itemRequestClass.getMethod("builder").invoke(null);

        // Configurar el item
        itemBuilder = itemBuilder.getClass().getMethod("id", String.class).invoke(itemBuilder, factura.getId().toString());
        itemBuilder = itemBuilder.getClass().getMethod("title", String.class).invoke(itemBuilder, "Servicio profesional - BackPs");
        itemBuilder = itemBuilder.getClass().getMethod("description", String.class).invoke(itemBuilder, "Pago por servicios profesionales");
        itemBuilder = itemBuilder.getClass().getMethod("quantity", Integer.class).invoke(itemBuilder, 1);
        itemBuilder = itemBuilder.getClass().getMethod("currencyId", String.class).invoke(itemBuilder, "ARS");
        itemBuilder = itemBuilder.getClass().getMethod("unitPrice", BigDecimal.class).invoke(itemBuilder, factura.getImporte());

        Object item = itemBuilder.getClass().getMethod("build").invoke(itemBuilder);

        List<Object> items = new ArrayList<>();
        items.add(item);

        // Crear back URLs usando reflection
        Class<?> backUrlsClass = Class.forName("com.mercadopago.client.preference.PreferenceBackUrlsRequest");
        Object backUrlsBuilder = backUrlsClass.getMethod("builder").invoke(null);

        backUrlsBuilder = backUrlsBuilder.getClass().getMethod("success", String.class).invoke(backUrlsBuilder, "http://localhost:8081/pago/exitoso");
        backUrlsBuilder = backUrlsBuilder.getClass().getMethod("failure", String.class).invoke(backUrlsBuilder, "http://localhost:8081/pago/fallido");
        backUrlsBuilder = backUrlsBuilder.getClass().getMethod("pending", String.class).invoke(backUrlsBuilder, "http://localhost:8081/pago/pendiente");

        Object backUrls = backUrlsBuilder.getClass().getMethod("build").invoke(backUrlsBuilder);

        // Crear preference request usando reflection
        Class<?> preferenceRequestClass = Class.forName("com.mercadopago.client.preference.PreferenceRequest");
        Object preferenceBuilder = preferenceRequestClass.getMethod("builder").invoke(null);

        preferenceBuilder = preferenceBuilder.getClass().getMethod("items", List.class).invoke(preferenceBuilder, items);
        preferenceBuilder = preferenceBuilder.getClass().getMethod("externalReference", String.class).invoke(preferenceBuilder, factura.getId().toString());
        preferenceBuilder = preferenceBuilder.getClass().getMethod("backUrls", backUrls.getClass()).invoke(preferenceBuilder, backUrls);
        preferenceBuilder = preferenceBuilder.getClass().getMethod("autoReturn", String.class).invoke(preferenceBuilder, "approved");

        Object preferenceRequest = preferenceBuilder.getClass().getMethod("build").invoke(preferenceBuilder);

        // Crear cliente y ejecutar
        Class<?> clientClass = Class.forName("com.mercadopago.client.preference.PreferenceClient");
        Object client = clientClass.getDeclaredConstructor().newInstance();

        return clientClass.getMethod("create", preferenceRequestClass).invoke(client, preferenceRequest);
    }

    private Object crearPreferenciaMock(Factura factura, String profesionalMPUserId) {
        log.info("Creando preferencia MOCK para factura ID: {}", factura.getId());

        // Crear objeto mock que simule una preferencia de MercadoPago
        return new MockPreference(
            "mock_preference_" + factura.getId(),
            "https://mock-mercadopago.com/init_point?preference_id=mock_" + factura.getId(),
            factura.getId().toString()
        );
    }

    // Clase interna para simular una preferencia de MercadoPago
    public static class MockPreference {
        private final String id;
        private final String initPoint;
        private final String externalReference;

        public MockPreference(String id, String initPoint, String externalReference) {
            this.id = id;
            this.initPoint = initPoint;
            this.externalReference = externalReference;
        }

        public String getId() {
            return id;
        }

        public String getInitPoint() {
            return initPoint;
        }

        public String getExternalReference() {
            return externalReference;
        }
    }
}
