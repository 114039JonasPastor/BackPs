# 🔧 URGENTE: Configuración Incorrecta de Email en Render

## ❌ Problema Identificado

Tu configuración en Render usa **puerto 465 (SSL directo)** pero el código está preparado para **puerto 587 (STARTTLS)**.

### Variables INCORRECTAS en Render:
```
SPRING_MAIL_PORT = 465  ❌
SPRING_MAIL_PROPERTIES_MAIL_SMTP_SSL_ENABLE = true  ❌
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE = false  ❌
```

### Variables CORRECTAS para Gmail:
```
SPRING_MAIL_PORT = 587  ✅
SPRING_MAIL_PROPERTIES_MAIL_SMTP_SSL_ENABLE = false  ✅
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE = true  ✅
```

## 🚀 Solución Rápida

Ve a: **https://dashboard.render.com**

### Paso 1: Selecciona el servicio `tuoficio-backend`

### Paso 2: Ve a la pestaña `Environment`

### Paso 3: Cambia estas 3 variables:

| Variable | Valor Actual (MALO) | Valor Correcto |
|----------|---------------------|----------------|
| `SPRING_MAIL_PORT` | 465 | **587** |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_SSL_ENABLE` | true | **false** |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE` | false | **true** |

### Paso 4: Guarda los cambios

Click en **"Save Changes"**

### Paso 5: Redeploy Manual

⚠️ **MUY IMPORTANTE:** Después de cambiar variables, debes hacer **Manual Deploy**

1. Ve a la pestaña **"Manual Deploy"**
2. Click en **"Deploy latest commit"**
3. Espera 5-10 minutos a que termine el deploy

## 📋 Variables COMPLETAS para Render

Copia y pega estas configuraciones exactamente:

```env
# Email Configuration - Gmail
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=tuoficiopracticasupervisada@gmail.com
SPRING_MAIL_PASSWORD=twzflugxxoydaswk
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_SSL_ENABLE=false
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true

# Email alternativo (sin prefijo SPRING_)
EMAIL_USERNAME=tuoficiopracticasupervisada@gmail.com
EMAIL_PASSWORD=twzflugxxoydaswk

# URLs
BACKEND_URL=https://tuoficio-backend.onrender.com
FRONTEND_URL=https://tuoficio-frontend.onrender.com
```

## 🔍 ¿Por qué este cambio?

Gmail recomienda usar:
- **Puerto 587** con STARTTLS (más moderno y compatible)
- En lugar de puerto 465 con SSL directo (legacy)

Tu application.yml está configurado para 587, por eso el email no funciona en producción.

## ✅ Verificación Post-Deploy

Después del redeploy:

1. **Prueba el registro:**
   - Ve a: https://tuoficio-frontend.onrender.com/auth/registro
   - Registra un usuario de prueba

2. **Verifica los logs:**
   - Render Dashboard → tuoficio-backend → Logs
   - Busca: `✅ Email enviado exitosamente`

3. **Reenvía email al usuario afectado:**
   ```bash
   curl -X POST 'https://tuoficio-backend.onrender.com/api/v1/registro/resend-confirmation?email=hopemap330@muhaos.com'
   ```

## 📞 Si Sigue Sin Funcionar

Revisa los logs de Render y busca:
- "Authentication failed" → Problema con el App Password
- "Connection timeout" → Problema de firewall/red
- "Mail server connection failed" → Problema de configuración

---

**Tiempo estimado de solución:** 15 minutos (cambiar variables + redeploy)
