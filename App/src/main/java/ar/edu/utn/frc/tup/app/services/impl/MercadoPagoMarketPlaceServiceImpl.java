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

    @Value("${mercadopago.access.token:TEST-126586428349320-110318-0380962aaaf2f8b76bc913c0fb8aa744-419753663}")
    private String accessToken;

    @PostConstruct
    public void init() {
        try {
            log.info("=== INICIALIZANDO MERCADOPAGO ===");

            // Validar tipo de token
            if (accessToken.equals("MOCK_TOKEN")) {
                log.warn("⚠️ MODO MOCK ACTIVADO - No se conectará a MercadoPago real");
                log.warn("Para usar MercadoPago Sandbox necesitas:");
                log.warn("1. Ir a https://www.mercadopago.com.ar/developers/panel/app");
                log.warn("2. Crear una aplicación");
                log.warn("3. Ir a 'Credenciales' → 'Credenciales de prueba'");
                log.warn("4. Copiar el Access Token que empieza con TEST-");
                return;
            }

            if (accessToken.startsWith("APP_USR-")) {
                log.error("❌ ERROR: Token de PRODUCCIÓN detectado");
                log.error("Las credenciales APP_USR- son para PRODUCCIÓN, no para desarrollo");
                log.error("Necesitas credenciales de SANDBOX que empiecen con TEST-");
                log.warn("Funcionando en MODO MOCK hasta configurar credenciales correctas");
                accessToken = "MOCK_TOKEN"; // Forzar modo mock
                return;
            }

            if (!accessToken.startsWith("TEST-")) {
                log.error("❌ ERROR: Token debe ser de SANDBOX (empezar con TEST-)");
                log.error("Token actual: {}...", accessToken.substring(0, 12));
                log.warn("Funcionando en MODO MOCK");
                accessToken = "MOCK_TOKEN";
                return;
            }

            log.info("Token de SANDBOX configurado: {}...", accessToken.substring(0, 12));

            Class<?> configClass = Class.forName("com.mercadopago.MercadoPagoConfig");

            // Verificar estado actual
            java.lang.reflect.Method getTokenMethod = configClass.getMethod("getAccessToken");
            String tokenActual = (String) getTokenMethod.invoke(null);
            log.info("Token anterior en SDK: {}", tokenActual != null ? tokenActual.substring(0, 12) + "..." : "NULL");

            // Configurar token de sandbox
            java.lang.reflect.Method setTokenMethod = configClass.getMethod("setAccessToken", String.class);
            setTokenMethod.invoke(null, accessToken);

            // Validar configuración
            String configuredToken = (String) getTokenMethod.invoke(null);
            log.info("Token verificado en SDK: {}",
                    configuredToken != null ? configuredToken.substring(0, 12) + "..." : "NULL");

            if (configuredToken != null && configuredToken.equals(accessToken)) {
                log.info("✅ MercadoPago SANDBOX configurado correctamente");
            } else {
                log.error("❌ Token no coincide después de configurar");
            }

        } catch (Exception e) {
            log.error("❌ Error configurando MercadoPago: {}", e.getMessage());
            log.error("Stack trace:", e);
        }
    }


    private void asegurarConfiguracion() {
        // Si está en modo mock, no hacer nada
        if (accessToken.equals("MOCK_TOKEN") || accessToken.startsWith("APP_USR-")) {
            return;
        }

        try {
            Class<?> configClass = Class.forName("com.mercadopago.MercadoPagoConfig");
            java.lang.reflect.Method getTokenMethod = configClass.getMethod("getAccessToken");
            String tokenActual = (String) getTokenMethod.invoke(null);

            if (tokenActual == null || !tokenActual.equals(accessToken)) {
                log.warn("Reconfigurando token MP - Actual: {}, Esperado: {}",
                        tokenActual != null ? tokenActual.substring(0, 12) + "..." : "NULL",
                        accessToken.substring(0, 12) + "...");

                java.lang.reflect.Method setTokenMethod = configClass.getMethod("setAccessToken", String.class);
                setTokenMethod.invoke(null, accessToken);
            }
        } catch (Exception e) {
            log.error("Error al asegurar configuración MP: {}", e.getMessage());
        }
    }

    @Override
    public Object crearPreferenciaMarketPlace(Factura factura, String profesionalMPUserId) {
        // Si el token no es de sandbox, usar modo mock
        if (accessToken.equals("MOCK_TOKEN") || accessToken.startsWith("APP_USR-")) {
            log.warn("Usando modo MOCK - Token no es de sandbox");
            return crearPreferenciaMock(factura, profesionalMPUserId);
        }

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
        asegurarConfiguracion();

        BigDecimal comision = factura.getImporte().multiply(new BigDecimal("0.05"));

        // Crear item
        Class<?> itemRequestClass = Class.forName("com.mercadopago.client.preference.PreferenceItemRequest");
        Object itemBuilder = itemRequestClass.getMethod("builder").invoke(null);

        itemBuilder = itemBuilder.getClass().getMethod("id", String.class).invoke(itemBuilder, factura.getId().toString());
        itemBuilder = itemBuilder.getClass().getMethod("title", String.class).invoke(itemBuilder, "Servicio profesional - BackPs");
        itemBuilder = itemBuilder.getClass().getMethod("description", String.class).invoke(itemBuilder, "Pago por servicios profesionales");
        itemBuilder = itemBuilder.getClass().getMethod("quantity", Integer.class).invoke(itemBuilder, 1);
        itemBuilder = itemBuilder.getClass().getMethod("currencyId", String.class).invoke(itemBuilder, "ARS");
        itemBuilder = itemBuilder.getClass().getMethod("unitPrice", BigDecimal.class).invoke(itemBuilder, factura.getImporte());

        Object item = itemBuilder.getClass().getMethod("build").invoke(itemBuilder);

        List<Object> items = new ArrayList<>();
        items.add(item);

        // Crear back URLs
        Class<?> backUrlsClass = Class.forName("com.mercadopago.client.preference.PreferenceBackUrlsRequest");
        Object backUrlsBuilder = backUrlsClass.getMethod("builder").invoke(null);

        backUrlsBuilder = backUrlsBuilder.getClass().getMethod("success", String.class).invoke(backUrlsBuilder, "http://localhost:8081/api/v1/pagos/success");
        backUrlsBuilder = backUrlsBuilder.getClass().getMethod("failure", String.class).invoke(backUrlsBuilder, "http://localhost:8081/api/v1/pagos/failure");
        backUrlsBuilder = backUrlsBuilder.getClass().getMethod("pending", String.class).invoke(backUrlsBuilder, "http://localhost:8081/api/v1/pagos/pending");

        Object backUrls = backUrlsBuilder.getClass().getMethod("build").invoke(backUrlsBuilder);

        // Crear preference request
        Class<?> preferenceRequestClass = Class.forName("com.mercadopago.client.preference.PreferenceRequest");
        Object preferenceBuilder = preferenceRequestClass.getMethod("builder").invoke(null);

        preferenceBuilder = preferenceBuilder.getClass().getMethod("items", List.class).invoke(preferenceBuilder, items);
        preferenceBuilder = preferenceBuilder.getClass().getMethod("externalReference", String.class).invoke(preferenceBuilder, "FACTURA_" + factura.getId());
        preferenceBuilder = preferenceBuilder.getClass().getMethod("backUrls", backUrls.getClass()).invoke(preferenceBuilder, backUrls);

        // ✅ AGREGAR: Configurar para que acepte pagos sin login
        preferenceBuilder = preferenceBuilder.getClass().getMethod("binaryMode", Boolean.class).invoke(preferenceBuilder, true);

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
