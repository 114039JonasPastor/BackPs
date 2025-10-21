package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.entities.Factura;
import ar.edu.utn.frc.tup.app.services.MercadoPagoMarketPlaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Implementación temporal/mock para desarrollo cuando MercadoPago SDK no está disponible
 */
@Service
@Slf4j
@Profile("!production")
@ConditionalOnMissingBean(name = "mercadoPagoMarketPlaceServiceImpl")
public class MercadoPagoMockServiceImpl implements MercadoPagoMarketPlaceService {

    @Override
    public Object crearPreferenciaMarketPlace(Factura factura, String profesionalMPUserId) {
        log.warn("Usando implementación MOCK de MercadoPago - Solo para desarrollo");
        log.info("Creando preferencia mock para factura ID: {} por monto: {}",
                factura.getId(), factura.getImporte());

        // Crear un objeto mock que simule la respuesta de MercadoPago
        return new MockPreference(
                "mock_preference_" + factura.getId(),
                "https://mock-mercadopago.com/init_point?preference_id=mock_" + factura.getId(),
                factura.getId().toString()
        );
    }

    // Clase interna para simular Preference de MercadoPago
    public static class MockPreference {
        private final String id;
        private final String initPoint;
        private final String externalReference;

        public MockPreference(String id, String initPoint, String externalReference) {
            this.id = id;
            this.initPoint = initPoint;
            this.externalReference = externalReference;
        }

        public String getId() { return id; }
        public String getInitPoint() { return initPoint; }
        public String getExternalReference() { return externalReference; }
    }
}
