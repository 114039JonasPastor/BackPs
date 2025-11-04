# ✅ Verificación MercadoPago SANDBOX Configurado

## Credenciales Configuradas

### Access Token (TEST-)
```
TEST-126586428349320-110318-0380962aaaf2f8b76bc913c0fb8aa744-419753663
```

### Public Key (TEST-)
```
TEST-935b4d34-551e-4038-8d5b-1177744fbd63
```

## ✅ Estado: CONFIGURADO CORRECTAMENTE

Tus credenciales son de **SANDBOX** (modo prueba) y están correctamente configuradas.

---

## 📋 Pasos para verificar que funciona

### 1. Reinicia la aplicación

Al iniciar, deberías ver en los logs:

```
=== INICIALIZANDO MERCADOPAGO ===
Token de SANDBOX configurado: TEST-1265864...
Token anterior en SDK: NULL
Token verificado en SDK: TEST-1265864...
✅ MercadoPago SANDBOX configurado correctamente
```

### 2. Verifica la configuración

**Endpoint:** `GET http://localhost:8081/api/v1/pagos/debug-config`

**Respuesta esperada:**
```json
{
  "accessToken_configured": true,
  "accessToken_prefix": "TEST-1265864...",
  "mode": "SANDBOX"
}
```

### 3. Prueba crear una preferencia

**Endpoint:** `GET http://localhost:8081/api/v1/pagos/test-simple-preference`

**Respuesta esperada:** 
- ✅ Status 200 con un objeto Preference real de MercadoPago
- ❌ Ya NO debería devolver error 401

### 4. Genera una factura y crea una preferencia de pago

**Paso 1 - Generar factura:**
```http
POST http://localhost:8081/api/v1/facturas
Content-Type: application/json

{
  "importe": 10000,
  "profesionalId": 1,
  "clienteId": 1
}
```

**Respuesta:**
```json
{
  "facturaId": 1,
  "importe": 10000,
  "mensaje": "Factura generada exitosamente"
}
```

**Paso 2 - Crear preferencia de pago:**
```http
POST http://localhost:8081/api/v1/pagos/comprar
Content-Type: application/json

{
  "idUsuario": 1,
  "idProfesional": 1,
  "importe": 10000
}
```

**Respuesta esperada:**
```json
{
  "preference_id": "2935832272-xxxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "init_point": "https://www.mercadopago.com.ar/checkout/v1/redirect?pref_id=...",
  "sandbox_init_point": "https://sandbox.mercadopago.com.ar/checkout/v1/redirect?pref_id=...",
  "facturaId": 1,
  "importe": 10000,
  "message": "Compra lista para procesar"
}
```

### 5. Simula un pago en Sandbox

1. **Abre el `sandbox_init_point`** en tu navegador
2. **Completa el checkout como invitado** (NO inicies sesión)
3. **Usa una tarjeta de prueba:**
   - Número: `4509 9535 6623 3704` (Visa aprobada)
   - CVV: `123`
   - Vencimiento: `11/25`
   - Nombre: `APRO`
   - DNI: Cualquier número
4. **Confirma el pago**

### 6. Webhook automático (opcional)

Si configuraste ngrok, cuando el pago se complete, MercadoPago enviará un webhook a:
```
https://unrabbeted-chi-clerkly.ngrok-free.dev/api/v1/pagos/webhook
```

Tu aplicación recibirá el evento y actualizará el estado de la factura automáticamente.

---

## 🎯 Diferencias con modo MOCK

| Característica | MOCK (anterior) | SANDBOX (actual) |
|---------------|-----------------|-------------------|
| Conexión a MP API | ❌ No | ✅ Sí |
| Checkout real | ❌ No | ✅ Sí |
| Tarjetas de prueba | ❌ No | ✅ Sí |
| Webhooks | ❌ No | ✅ Sí |
| Dinero real | ❌ No | ❌ No |
| Requiere credenciales | ❌ No | ✅ Sí (TEST-) |

---

## 🔧 Tarjetas de prueba disponibles

### ✅ Aprobadas
- **Visa:** `4509 9535 6623 3704`
- **Mastercard:** `5031 7557 3453 0604`
- **Nombre:** `APRO`

### ❌ Rechazadas
- **Visa:** `4013 5406 8274 6260`
- **Nombre:** `OTHE`

### 💳 Fondos insuficientes
- **Visa:** `4235 6477 2802 5682`
- **Nombre:** `FUND`

### 📝 Datos comunes para todas
- **CVV:** `123`
- **Vencimiento:** `11/25` (cualquier fecha futura)
- **DNI:** Cualquier número

---

## 🚨 Solución de problemas

### Si ves "MODO MOCK ACTIVADO"
- Verifica que las credenciales en `.env` o `application.yml` empiecen con `TEST-`
- Reinicia la aplicación completamente

### Si obtienes error 401
- Verifica que las credenciales estén bien copiadas (sin espacios extra)
- Confirma que sean de "Credenciales de prueba" en el panel de MercadoPago

### Si el checkout no carga
- Verifica que el `sandbox_init_point` sea de `sandbox.mercadopago.com.ar`
- Usa modo incógnito en el navegador
- Asegúrate de NO iniciar sesión, usa el checkout como invitado

---

## 📚 Próximos pasos

Con SANDBOX funcionando puedes:

1. ✅ Simular compras completas sin mover dinero real
2. ✅ Probar diferentes estados de pago (aprobado, rechazado, pendiente)
3. ✅ Recibir webhooks cuando cambie el estado del pago
4. ✅ Integrar con tu frontend
5. ✅ Probar el flujo completo de la aplicación

### Cuando estés listo para producción

1. Obtén las credenciales de **PRODUCCIÓN** (las `APP_USR-` que ya tienes guardadas)
2. Completa la homologación de MercadoPago
3. Configura cuenta bancaria para recibir pagos reales
4. Cambia las credenciales en producción

---

## 🎉 ¡Todo listo!

Tu aplicación ahora está conectada al **entorno SANDBOX de MercadoPago** y puedes simular pagos reales sin riesgo.

