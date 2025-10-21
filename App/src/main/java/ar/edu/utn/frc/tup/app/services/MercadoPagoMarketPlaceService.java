package ar.edu.utn.frc.tup.app.services;

import ar.edu.utn.frc.tup.app.entities.Factura;

public interface MercadoPagoMarketPlaceService {
    Object crearPreferenciaMarketPlace(Factura factura, String profesionalMPUserId);
}