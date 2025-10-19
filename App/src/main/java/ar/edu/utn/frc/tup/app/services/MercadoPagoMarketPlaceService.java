package ar.edu.utn.frc.tup.app.services;

import ar.edu.utn.frc.tup.app.entities.Factura;
import com.mercadopago.resources.preference.Preference;

public interface MercadoPagoMarketPlaceService {
    Preference crearPreferenciaMarketPlace(Factura factura, String profesionalMPUserId);
}