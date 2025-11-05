package ar.edu.utn.frc.tup.app.dtos.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CardPaymentRequest {
    // Datos de negocio
    private BigDecimal importe;
    private Integer profesionalId;
    private Integer clienteId;

    // Datos de pago (Payments API)
    private String token;              // card_token generado en el frontend
    private Integer installments;      // cuotas (p.ej. 1)
    private String paymentMethodId;    // p.ej. "visa"
    private String email;              // email del pagador
    private String docType;            // p.ej. "DNI"
    private String docNumber;          // p.ej. "12345678"
}
