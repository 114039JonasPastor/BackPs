package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.entities.Factura;
import ar.edu.utn.frc.tup.app.services.MercadoPagoMarketPlaceService;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MercadoPagoMarketPlaceServiceImpl implements MercadoPagoMarketPlaceService {

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    @Override
    public Preference crearPreferenciaMarketPlace(Factura factura, String profesionalMPUserId) {
        try {
            BigDecimal comision = factura.getImporte().multiply(new BigDecimal("0.05"));

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .id(factura.getId().toString())
                    .title("Servicio profesional - BackPs")
                    .description("Pago por servicios profesionales")
                    .quantity(1)
                    .currencyId("ARS")
                    .unitPrice(factura.getImporte())
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(List.of(item))
                    .externalReference(factura.getId().toString())
                    .marketplace("BACKPS_MARKETPLACE")
                    .marketplaceFee(comision)
                    .backUrls(PreferenceBackUrlsRequest.builder()
                            .success("http://localhost:8081/pago/exitoso")
                            .failure("http://localhost:8081/pago/fallido")
                            .pending("http://localhost:8081/pago/pendiente")
                            .build())
                    .autoReturn("approved")
                    .build();

            PreferenceClient client = new PreferenceClient();
            return client.create(preferenceRequest);

        } catch (MPException | MPApiException e) {
            throw new RuntimeException("Error al crear preferencia de MercadoPago: " + e.getMessage(), e);
        }
    }
}

