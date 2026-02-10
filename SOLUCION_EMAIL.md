# 🔧 Solución: Email de Confirmación No Se Envía

## 📋 Problema

Los usuarios se registran exitosamente pero:
- ✅ Se crea el usuario en la base de datos con `active = false`
- ❌ No reciben el email de confirmación
- ❌ No pueden hacer login (cuenta inactiva)

## 🎯 Soluciones Rápidas

### Solución 1: Reenviar Email de Confirmación (Recomendado)

Ya existe un endpoint para reenviar el email:

**Para Producción:**
```bash
curl -X POST 'https://tuoficio-backend.onrender.com/api/v1/registro/resend-confirmation?email=usuario@ejemplo.com'
```

**Para Local:**
```bash
curl -X POST 'http://localhost:8081/api/v1/registro/resend-confirmation?email=usuario@ejemplo.com'
```

**O usa el script PowerShell:**
```powershell
.\test-email-local.ps1
```

### Solución 2: Activar Usuario Manualmente (Temporal)

Si el email sigue sin funcionar, activa al usuario directamente en la base de datos:

```sql
UPDATE auth 
SET active = true 
WHERE mail = 'usuario@ejemplo.com';
```

**O usa el script PowerShell:**
```powershell
.\reactivar-usuario.ps1
```

## 🔍 Causa Raíz del Problema

El problema está en la configuración de las variables de entorno en **Render**:

### 1️⃣ Verificar Variables en Render

Ve a: https://dashboard.render.com
- Selecciona el servicio: **tuoficio-backend**
- Ve a la pestaña: **Environment**

**Variables requeridas:**
```env
EMAIL_USERNAME=tuoficiopracticasupervisada@gmail.com
EMAIL_PASSWORD=twzf lugx xoyd aswk
BACKEND_URL=https://tuoficio-backend.onrender.com
FRONTEND_URL=https://tuoficio-frontend.onrender.com
```

⚠️ **IMPORTANTE:** Después de agregar/modificar variables, debes hacer **REDEPLOY manual**

### 2️⃣ Verificar Gmail App Password

La contraseña debe ser un **App Password** de Gmail (no la contraseña normal):

1. Ve a: https://myaccount.google.com/apppasswords
2. Login con: `tuoficiopracticasupervisada@gmail.com`
3. Verifica que existe el App Password o crea uno nuevo
4. Si creas uno nuevo, actualiza `EMAIL_PASSWORD` en Render

**Formato correcto:** 16 caracteres sin espacios (ej: `twzflugxxoydaswk`)

### 3️⃣ Ver Logs de Render

Después de hacer cambios, monitorea los logs:

1. Ve a Render Dashboard → tuoficio-backend → **Logs**
2. Registra un usuario de prueba
3. Busca estos mensajes:

**Éxito:**
```
📧 Enviando email de confirmación a: usuario@ejemplo.com
✅ Email enviado exitosamente
```

**Error:**
```
⚠️⚠️⚠️ ERROR CRÍTICO: No se pudo enviar el email de confirmación
```

## 🛠️ Scripts Disponibles

### `test-email-local.ps1`
- Verifica las variables de entorno locales
- Permite probar el reenvío de email (local y producción)
- Muestra instrucciones para verificar Render

### `reactivar-usuario.ps1`
- Genera SQL para activar manualmente un usuario
- Útil como solución temporal mientras arreglas el email
- Guarda el SQL en un archivo

### `test-registro-email.ps1` (ya existente)
- Prueba completa del registro de usuario

## 📝 Checklist de Solución

- [ ] Verificar variables de entorno en Render
- [ ] Verificar Gmail App Password está activo
- [ ] Redeploy del backend en Render
- [ ] Probar registro con email de prueba
- [ ] Verificar logs de Render
- [ ] Si funciona, reenviar confirmación a usuarios afectados
- [ ] Si no funciona, considerar cambiar a SendGrid/Resend

## 🚀 Alternativa: Usar Otro Servicio de Email

Si Gmail sigue fallando, considera usar servicios más confiables para producción:

### Resend (Recomendado)
- Free tier: 100 emails/día
- Fácil integración
- Más confiable para producción
- Website: https://resend.com

### SendGrid
- Free tier: 100 emails/día
- Integración con Spring Boot
- Website: https://sendgrid.com

## 📞 Soporte

Si el problema persiste:
1. Revisa los logs completos de Render
2. Busca el error específico (AUTH_FAILED, TIMEOUT, etc.)
3. Consulta: [TROUBLESHOOTING_EMAIL.md](./TROUBLESHOOTING_EMAIL.md)
