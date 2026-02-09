# Fix: Email de Confirmación No Se Envía en Producción

## 🔍 Problema Identificado

Los usuarios no reciben el email de confirmación al registrarse en producción (https://tuoficio-frontend.onrender.com/auth/registro).

## 🛠️ Cambios Implementados

### 1. **Verificación de Cuenta Activa en Login** ✅
**Archivo**: `AuthServiceImpl.java`

Ahora el sistema **bloquea el login** de usuarios con email no confirmado:
```java
if (!auth.getActive()) {
    throw new RuntimeException("Cuenta no verificada. Por favor, revisa tu correo electrónico...");
}
```

### 2. **Logging Mejorado para Emails** ✅
**Archivo**: `EmailServiceImpl.java`

Se agregó logging detallado para diagnosticar problemas de envío:
- ✅ Configuración SMTP (host, port, from, to)
- ✅ Mensajes de error detallados con stack traces
- ✅ Información sobre el tipo de error

### 3. **Logging Mejorado en Registro** ✅
**Archivo**: `RegistroServiceImpl.java`

Se agregó tracking de estado de envío de emails:
- ⚠️ Alertas críticas cuando el email falla
- 📧 Link de confirmación en logs para uso manual
- ✅ Confirmación de envío exitoso

### 4. **Endpoint para Reenviar Confirmación** ✅
**Nuevo endpoint**: `POST /api/v1/registro/resend-confirmation?email={email}`

Permite a los usuarios solicitar el reenvío del email de confirmación.

### 5. **URLs Dinámicas de Producción** ✅
**Archivos**: `RegistroController.java`

Ahora las URLs en las páginas HTML de confirmación usan las variables de entorno:
- `FRONTEND_URL` para links a login y home
- `BACKEND_URL` para el link de confirmación

### 6. **FRONTEND_URL en render.yaml** ✅
Se configuró el valor correcto en `render.yaml`:
```yaml
- key: FRONTEND_URL
  value: https://tuoficio-frontend.onrender.com
```

---

## 🚀 Pasos para Desplegar y Resolver el Problema

### **Paso 1: Verificar Variables de Entorno en Render**

1. Ve a: https://dashboard.render.com
2. Selecciona el servicio **tuoficio-backend**
3. Ve a la pestaña **"Environment"**
4. Verifica que estas variables estén configuradas:

```env
EMAIL_USERNAME=tuoficiopracticasupervisada@gmail.com
EMAIL_PASSWORD=twzf lugx xoyd aswk
BACKEND_URL=https://tuoficio-backend.onrender.com
FRONTEND_URL=https://tuoficio-frontend.onrender.com
```

5. Si faltan, agrégalas manualmente
6. **Guarda los cambios**

### **Paso 2: Verificar Gmail App Password**

La contraseña `twzf lugx xoyd aswk` debe ser un **App Password activo**:

1. Ve a: https://myaccount.google.com/apppasswords
2. Login con: `tuoficiopracticasupervisada@gmail.com`
3. Si el app password no existe o fue revocado, crea uno nuevo:
   - Nombre: "Render Backend TuOficio"
   - Copia la contraseña generada (16 caracteres)
4. Si creaste uno nuevo, **actualiza** `EMAIL_PASSWORD` en Render

### **Paso 3: Desplegar los Cambios**

#### Opción A: Deploy Automático (si tienes CI/CD)
```bash
cd c:\Users\jonas\OneDrive\Documentos\GitHub\BackPs
git add .
git commit -m "Fix: Email confirmation not being sent in production"
git push origin main
```

#### Opción B: Deploy Manual en Render
1. Ve a Render Dashboard → tuoficio-backend
2. Haz clic en **"Manual Deploy"** → **"Deploy latest commit"**
3. Espera a que termine el despliegue (5-10 minutos)

### **Paso 4: Verificar los Logs**

Una vez desplegado:

1. Ve a Render Dashboard → tuoficio-backend → **Logs**
2. Haz un registro de prueba en: https://tuoficio-frontend.onrender.com/auth/registro
3. Busca en los logs:
   - ✅ `"📧 Enviando email de confirmación a:"`
   - ✅ `"✅ Email enviado exitosamente"`
   - ❌ `"⚠️⚠️⚠️ ERROR CRÍTICO: No se pudo enviar el email"`

### **Paso 5: Prueba el Reenvío (si es necesario)**

Si un usuario no recibió el email, puedes usar el nuevo endpoint:

**Solicitud HTTP:**
```http
POST https://tuoficio-backend.onrender.com/api/v1/registro/resend-confirmation?email=usuario@example.com
```

**Con cURL:**
```bash
curl -X POST "https://tuoficio-backend.onrender.com/api/v1/registro/resend-confirmation?email=usuario@example.com"
```

**Respuesta exitosa:**
```json
"Email de confirmación reenviado exitosamente. Revisa tu bandeja de entrada."
```

---

## 🔍 Diagnóstico de Errores Comunes

### Error: "Authentication failed (535)"
**Causa**: Credenciales incorrectas
**Solución**:
- Verifica EMAIL_USERNAME y EMAIL_PASSWORD en Render
- Regenera el App Password de Gmail
- Asegúrate de que no haya espacios en el password

### Error: "Connection timeout"
**Causa**: Firewall bloqueando puerto 587
**Solución**:
- Render debería permitir SMTP por defecto
- Verifica la configuración en `application.yml`
- Considera usar SendGrid como alternativa

### Error: "Host not found"
**Causa**: Variable de entorno mal configurada
**Solución**:
- Verifica que `spring.mail.host=smtp.gmail.com`
- Revisa los logs de startup del backend

### Los usuarios no pueden hacer login después de registrarse
**Causa**: Email no confirmado (cuenta inactiva)
**Solución**:
- Usa el endpoint de reenvío de confirmación
- O activa manualmente la cuenta en la base de datos:
  ```sql
  UPDATE auth SET active = true WHERE mail = 'usuario@example.com';
  ```

---

## 📧 Alternativa: Migrar a SendGrid

Si Gmail sigue dando problemas, considera SendGrid (más confiable):

1. **Regístrate en SendGrid**: https://sendgrid.com/ (gratis hasta 100 emails/día)
2. **Obtén API Key**
3. **Actualiza las dependencias** en `pom.xml`:
   ```xml
   <dependency>
       <groupId>com.sendgrid</groupId>
       <artifactId>sendgrid-java</artifactId>
       <version>4.9.3</version>
   </dependency>
   ```
4. **Actualiza `EmailServiceImpl`** para usar SendGrid API
5. **Configura en Render**:
   ```env
   SENDGRID_API_KEY=tu_api_key_aqui
   ```

---

## ✅ Checklist de Verificación

- [ ] Variables de entorno configuradas en Render
- [ ] Gmail App Password válido y activo
- [ ] Código desplegado en producción
- [ ] Logs muestran envío exitoso de emails
- [ ] Prueba de registro completada exitosamente
- [ ] Email recibido en bandeja de entrada
- [ ] Link de confirmación funciona correctamente
- [ ] Login bloqueado para cuentas no verificadas
- [ ] Endpoint de reenvío funciona correctamente

---

## 📝 Documentación de Referencia

- [Gmail App Passwords](https://support.google.com/accounts/answer/185833)
- [Render Environment Variables](https://render.com/docs/environment-variables)
- [Spring Mail Documentation](https://docs.spring.io/spring-framework/reference/integration/email.html)
- [SendGrid Java Integration](https://docs.sendgrid.com/for-developers/sending-email/v3-java-code-example)

---

## 🆘 Contacto de Soporte

Si el problema persiste después de seguir todos los pasos:

1. **Revisa los logs completos** en Render
2. **Busca el error específico** en los logs
3. **Copia el stack trace completo**
4. **Verifica la conexión SMTP** desde Render:
   ```bash
   telnet smtp.gmail.com 587
   ```

El logging mejorado te dará información detallada sobre cualquier fallo.
