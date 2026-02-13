# 🔗 Cómo Obtener tu URL de Render

## Método 1: Desde el Dashboard de Render

1. Ve a [Render Dashboard](https://dashboard.render.com)
2. Haz clic en tu servicio backend (ej: `tuoficio-backend`)
3. En la parte superior verás la URL de tu servicio, algo como:
   ```
   https://tuoficio-backend.onrender.com
   ```
4. Copia esa URL completa

## Método 2: Desde la página de tu servicio

1. Ve a [Render Dashboard](https://dashboard.render.com)
2. Selecciona tu servicio
3. En la pestaña "Settings"
4. Busca la sección que dice **"Service URL"** o similar
5. Copia la URL

## Método 3: Desde los logs

1. Ve a tu servicio en Render
2. Ve a la pestaña "Logs"
3. Cuando el servicio inicia, verás un mensaje como:
   ```
   Tomcat started on port(s): 8080 (http)
   Your service is live at https://tuoficio-backend.onrender.com
   ```

## 📝 Formato de la URL

La URL de Render siempre tiene este formato:
```
https://[nombre-del-servicio].onrender.com
```

Por ejemplo:
- `https://tuoficio-backend.onrender.com`
- `https://app-oficios-backend.onrender.com`
- `https://ps-backend.onrender.com`

## ✅ Verificar que funciona

Prueba accediendo a:
```
https://tu-url.onrender.com/actuator/health
```

Si el backend está funcionando, verás algo como:
```json
{
  "status": "UP"
}
```

## 🔧 Actualizar en los archivos

Una vez que tengas tu URL, actualízala en:

1. **test-admin.ps1**
   ```powershell
   $baseUrl = "https://TU-URL-AQUI.onrender.com"
   ```

2. **registrar-administrador.http**
   ```
   POST https://TU-URL-AQUI.onrender.com/api/v1/registro/administrador
   ```

3. **En Postman, Thunder Client, o cualquier cliente REST que uses**

## 🆘 ¿No tienes el backend desplegado aún?

Si aún no has desplegado el backend en Render, consulta:
- [DEPLOYMENT.md](DEPLOYMENT.md) - Guía completa de deployment
- [QUICK_START.md](QUICK_START.md) - Inicio rápido
