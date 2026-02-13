# Registrar Administrador - Guía de Uso

## ⚠️ IMPORTANTE: URL de Render
Reemplaza `https://tuoficio-backend.onrender.com` con tu URL real de Render.

Para obtener tu URL:
1. Ve a [Render Dashboard](https://dashboard.render.com)
2. Selecciona tu servicio backend
3. Copia la URL que aparece en la sección "Settings"

## 📋 Endpoint
```
POST /api/v1/registro/administrador
```

## 🔧 Métodos para Registrar un Administrador

### 0️⃣ PASO PREVIO: Verificar que el backend funciona
```powershell
cd C:\Users\jonas\OneDrive\Documentos\GitHub\BackPs
.\verificar-backend.ps1
```

Este script verifica:
- ✅ Que el backend en Render está funcionando
- ✅ Que el endpoint de administrador existe
- ✅ Te da un checklist de requisitos previos

---

### 1️⃣ Usando el archivo .http (VS Code con extensión REST Client)
Abre el archivo `registrar-administrador.http` y haz clic en "Send Request"

### 2️⃣ Usando PowerShell
```powershell
cd C:\Users\jonas\OneDrive\Documentos\GitHub\BackPs
.\test-admin.ps1
```

### 3️⃣ Usando curl (Git Bash o PowerShell)
```bash
curl -X POST https://tuoficio-backend.onrender.com/api/v1/registro/administrador \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin",
    "lastName": "Sistema",
    "mail": "admin@tuoficio.com",
    "password": "Admin123!",
    "idTipoDoc": 1,
    "documento": "12345678",
    "telefono": "3512345678",
    "nacimiento": "1990-01-01",
    "idBarrio": 1,
    "calle": "Av. Colón",
    "numero": "100"
  }'
```

### 4️⃣ Usando Postman
- **Method:** POST
- **URL:** https://tuoficio-backend.onrender.com/api/v1/registro/administrador
- **Headers:** Content-Type: application/json
- **Body (raw JSON):**
```json
{
  "name": "Admin",
  "lastName": "Sistema",
  "mail": "admin@tuoficio.com",
  "password": "Admin123!",
  "idTipoDoc": 1,
  "documento": "12345678",
  "telefono": "3512345678",
  "nacimiento": "1990-01-01",
  "idBarrio": 1,
  "calle": "Av. Colón",
  "numero": "100"
}
```

## ✅ Respuesta Exitosa (200 OK)
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "nombre": "Admin",
  "apellido": "Sistema",
  "email": "admin@tuoficio.com",
  "idUsuario": null,
  "documento": null,
  "telefono": null,
  "nacimiento": null,
  "idDireccion": null,
  "roles": ["ADMINISTRADOR"]
}
```

## ❌ Errores Comunes

### Email ya existe
```json
{
  "timestamp": "2026-02-13T...",
  "status": 400,
  "error": "Bad Request",
  "message": "El email ya está registrado"
}
```

### Tipo de documento no encontrado
```json
{
  "timestamp": "2026-02-13T...",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Tipo de documento no encontrado"
}
```

## 📝 Notas Importantes

1. **El administrador se crea activo:** A diferencia de usuarios normales, no requiere confirmación por email
2. **No crea registro en tabla Usuarios:** Solo crea en `auth` y `rolxusuario`
3. **Recibe token JWT inmediatamente:** Puede iniciar sesión de inmediato
4. **Rol asignado:** Se le asigna automáticamente el rol "ADMINISTRADOR"

## 🔒 Consideraciones de Seguridad

**IMPORTANTE:** Este endpoint debería estar protegido en producción para que solo:
- Administradores existentes puedan crear nuevos administradores, O
- Solo sea accesible durante la configuración inicial

Para protegerlo, agrega en `SecurityConfig.java`:
```java
.requestMatchers("/api/v1/registro/administrador").hasRole("ADMINISTRADOR")
```

## 🎯 Después de Crear el Admin

Puedes iniciar sesión usando:
- **Email:** admin@tuoficio.com
- **Password:** Admin123!
- **Endpoint:** POST /api/v1/auth/login

```bash
curl -X POST https://tuoficio-backend.onrender.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "mail": "admin@tuoficio.com",
    "password": "Admin123!"
  }'
```

## 🔧 Troubleshooting

### Error: "Tipo de documento no encontrado"
La tabla `tipos_documento` está vacía. Ejecuta en tu base de datos:
```sql
INSERT INTO tipos_documento (tipo) VALUES ('DNI'), ('CUIL'), ('PASAPORTE');
```

### Error: "Barrio no encontrado"
La tabla `barrios` está vacía. Ejecuta el script completo [ps.sql](ps.sql)

### Error: "Rol ADMINISTRADOR no encontrado"
La tabla `roles` no tiene los roles. Ejecuta:
```sql
INSERT INTO roles (descripcion) VALUES ('ADMINISTRADOR'), ('PROFESIONAL'), ('CLIENTE');
```

### Error: "Cannot connect to server"
- Verifica que tu URL de Render sea correcta
- Verifica que el backend esté desplegado y funcionando
- Ejecuta `.\verificar-backend.ps1` para diagnosticar

### Error: "El email ya está registrado"
El email ya existe en la base de datos. Opciones:
1. Usa otro email
2. Elimina el registro anterior:
```sql
DELETE FROM rolxusuario WHERE idauth = (SELECT idauth FROM auth WHERE mail = 'admin@tuoficio.com');
DELETE FROM auth WHERE mail = 'admin@tuoficio.com';
```

### El backend tarda mucho en responder (primera vez)
Render pone los servicios free en "sleep" después de 15 minutos de inactividad.
- La primera request puede tardar 30-60 segundos
- Espera pacientemente y volverá a activarse

## 📚 Recursos Adicionales

- [OBTENER_URL_RENDER.md](OBTENER_URL_RENDER.md) - Cómo encontrar tu URL de Render
- [DEPLOYMENT.md](DEPLOYMENT.md) - Guía completa de deployment
- [ps.sql](ps.sql) - Script completo de la base de datos

