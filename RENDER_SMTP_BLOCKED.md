# 🚫 Problema: Render Bloquea Conexiones SMTP

## ❌ Error en Logs

```
java.net.ConnectException: Operation timed out
Couldn't connect to host, port: smtp.gmail.com, 587; timeout -1
```

## 🔍 Causa Raíz

**Render bloquea conexiones SMTP salientes en los puertos 587 y 25** para prevenir spam y abuso. Esto es una política de seguridad estándar en muchos servicios cloud.

## ✅ Solución Aplicada: Puerto 465 (SSL)

He cambiado la configuración para usar **puerto 465 con SSL directo**, que suele estar permitido en Render:

### Cambios en application.yml:
```yaml
mail:
  port: 465  # Cambiado de 587
  properties:
    mail:
      smtp:
        ssl:
          enable: true   # Cambiado de false
        starttls:
          enable: false  # Cambiado de true
```

### Variables para Render (ya las tienes configuradas):
```env
SPRING_MAIL_PORT=465
SPRING_MAIL_PROPERTIES_MAIL_SMTP_SSL_ENABLE=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=false
SPRING_MAIL_USERNAME=tuoficiopracticasupervisada@gmail.com
SPRING_MAIL_PASSWORD=twzflugxxoydaswk
```

## 🚀 Siguiente Paso: Commit y Deploy

```bash
cd c:\Users\jonas\OneDrive\Documentos\GitHub\BackPs
git add App/src/main/resources/application.yml
git commit -m "fix: Cambiar a puerto 465 SSL para compatibilidad con Render"
git push origin main
```

Render hará redeploy automático (5-10 minutos).

## 🧪 Probar Después del Deploy

1. **Registra un usuario nuevo:**
   https://tuoficio-frontend.onrender.com/auth/registro

2. **Verifica los logs:**
   https://dashboard.render.com → tuoficio-backend → Logs
   
   Busca: `✅ Email enviado exitosamente`

3. **Si funciona, reactiva usuarios anteriores:**
   ```bash
   curl -X POST 'https://tuoficio-backend.onrender.com/api/v1/registro/resend-confirmation?email=hopemap330@muhaos.com'
   curl -X POST 'https://tuoficio-backend.onrender.com/api/v1/registro/resend-confirmation?email=didaje6940@muhaos.com'
   ```

## ⚠️ Si Puerto 465 Tampoco Funciona

Si Render también bloquea el puerto 465, necesitarás usar un servicio de email dedicado:

### Opción 1: Resend (Recomendado)
- ✅ **Free tier:** 3,000 emails/mes
- ✅ Fácil integración
- ✅ No requiere SMTP (usa API REST)
- ✅ Funciona en cualquier cloud
- 🌐 https://resend.com

**Implementación (15 minutos):**

1. Crear cuenta en Resend
2. Obtener API Key
3. Agregar dependencia Maven:
   ```xml
   <dependency>
       <groupId>com.resend</groupId>
       <artifactId>resend-java</artifactId>
       <version>3.0.0</version>
   </dependency>
   ```

4. Modificar `EmailServiceImpl`:
   ```java
   @Value("${resend.api.key}")
   private String resendApiKey;
   
   public void sendHtml(String to, String subject, String htmlBody) {
       Resend resend = new Resend(resendApiKey);
       
       SendEmailRequest request = SendEmailRequest.builder()
           .from("TuOficio <noreply@tuoficio.com>")
           .to(to)
           .subject(subject)
           .html(htmlBody)
           .build();
           
       resend.emails().send(request);
   }
   ```

### Opción 2: SendGrid
- ✅ Free tier: 100 emails/día
- ✅ API REST también
- 🌐 https://sendgrid.com

### Opción 3: Mailgun
- ✅ Free tier: 5,000 emails/mes (primeros 3 meses)
- 🌐 https://mailgun.com

## 📊 Comparativa

| Servicio | Free Tier | Tipo | Confiabilidad |
|----------|-----------|------|---------------|
| Gmail (puerto 465) | Ilimitado | SMTP | ⚠️ Puede fallar en cloud |
| **Resend** | 3,000/mes | API REST | ✅ Excelente |
| SendGrid | 100/día | API REST | ✅ Muy bueno |
| Mailgun | 5,000/mes | API REST | ✅ Muy bueno |

## 🎯 Recomendación Final

1. **Probar puerto 465 primero** (ya configurado)
2. Si no funciona en 1 día, **migrar a Resend** (solución definitiva)

---

**Documentación relacionada:**
- [Render Network Restrictions](https://render.com/docs/networking)
- [Resend Documentation](https://resend.com/docs)
