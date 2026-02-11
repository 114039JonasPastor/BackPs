# 🚀 Resend Implementado - Instrucciones de Deploy

## ✅ Cambios Realizados

### 1. **Dependencia de Resend agregada** (pom.xml)
```xml
<dependency>
    <groupId>com.resend</groupId>
    <artifactId>resend-java</artifactId>
    <version>3.0.0</version>
</dependency>
```

### 2. **EmailServiceImpl actualizado**
- ✅ Ya no usa JavaMailSender (SMTP)
- ✅ Ahora usa Resend API (REST)
- ✅ No necesita puertos SMTP (465/587)
- ✅ Funciona en cualquier cloud

### 3. **Configuración actualizada** (application.yml)
```yaml
resend:
  api:
    key: ${RESEND_API_KEY}
  from:
    email: ${RESEND_FROM_EMAIL:TuOficio <noreply@tuoficio.com>}
```

## 🔧 Configurar Variables en Render

### Paso 1: Ve a Render Dashboard
https://dashboard.render.com → `tuoficio-backend` → **Environment**

### Paso 2: Agrega estas 2 variables

```env
RESEND_API_KEY=re_XiogQm5V_SPV7tjv5JCDZ7wPDaMKAggWe
RESEND_FROM_EMAIL=TuOficio <noreply@tuoficio.com>
```

**IMPORTANTE:** El `RESEND_FROM_EMAIL` debe seguir este formato:
- ✅ `TuOficio <noreply@tuoficio.com>` (correcto)
- ❌ `noreply@tuoficio.com` (sin nombre, menos profesional)

### Paso 3: Guarda y Redeploy

1. Click en **"Save Changes"**
2. Ve a **"Manual Deploy"**
3. Click **"Deploy latest commit"**
4. Espera 5-10 minutos

## 🧪 Probar Después del Deploy

### 1. Registra un usuario de prueba
https://tuoficio-frontend.onrender.com/auth/registro

### 2. Verifica los logs
Render Dashboard → tuoficio-backend → **Logs**

**Busca estos mensajes:**
```
📧 Preparando email HTML para: usuario@ejemplo.com
📧 Usando Resend API (no SMTP)
📤 Enviando email HTML via Resend API...
✅ ¡Email HTML enviado exitosamente!
✅ Resend Email ID: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

### 3. Revisa tu email
Deberías recibir el email de confirmación en menos de 1 minuto.

## 📧 Reenviar Confirmación a Usuarios Anteriores

Una vez que funcione, reactiva los usuarios que no recibieron email:

```bash
# Usuario 1
curl -X POST 'https://tuoficio-backend.onrender.com/api/v1/registro/resend-confirmation?email=hopemap330@muhaos.com'

# Usuario 2
curl -X POST 'https://tuoficio-backend.onrender.com/api/v1/registro/resend-confirmation?email=didaje6940@muhaos.com'

# Usuario 3
curl -X POST 'https://tuoficio-backend.onrender.com/api/v1/registro/resend-confirmation?email=royana9988@newtrea.com'
```

## ⚙️ Configuración de Resend

### Verificar dominio (Opcional pero Recomendado)

Para mejor deliverability, verifica tu dominio en Resend:

1. Ve a: https://resend.com/domains
2. Click **"Add Domain"**
3. Ingresa tu dominio: `tuoficio.com` (o el que tengas)
4. Agrega los registros DNS que te indique Resend
5. Una vez verificado, cambia el email:
   ```env
   RESEND_FROM_EMAIL=TuOficio <noreply@tuoficio.com>
   ```

**Mientras tanto:** Puedes usar el dominio de prueba que Resend proporciona.

### Límites del Plan Gratuito

- ✅ **3,000 emails/mes**
- ✅ API REST ilimitadas
- ✅ Soporte por email
- ✅ Logs de envío (30 días)

## 🔍 Troubleshooting

### Error: "API key is invalid"
- Verifica que copiaste correctamente la API key
- Debe empezar con `re_`
- No incluyas espacios

### Error: "From email not verified"
- Si usas un dominio personalizado, debe estar verificado
- O usa: `onboarding@resend.dev` (dominio de prueba)

### Emails no llegan
1. Verifica logs de Resend: https://resend.com/emails
2. Revisa spam/correo no deseado
3. Verifica que el `RESEND_FROM_EMAIL` esté correcto

## 📊 Monitoreo

Puedes ver todos los emails enviados en:
https://resend.com/emails

Allí verás:
- Estado de envío
- Fecha/hora
- Destinatario
- ID del email
- Errores (si los hay)

---

**Ventajas de Resend vs SMTP:**
- ✅ No depende de puertos (funciona en cualquier cloud)
- ✅ Mejor deliverability
- ✅ Monitoreo en tiempo real
- ✅ Sin timeouts de conexión
- ✅ Mejor para producción
