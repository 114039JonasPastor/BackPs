# Deployment en Render - Tu Oficio

Este proyecto está configurado para deployarse en Render con tres servicios:
1. **Base de datos PostgreSQL**
2. **Backend Spring Boot**
3. **Frontend Angular**

## 📋 Pre-requisitos

- Cuenta en [Render.com](https://render.com)
- Repositorio en GitHub
- Credenciales de servicios externos (MercadoPago, Stream Chat, Email)

## 🚀 Pasos para el Deployment

### 1. Preparar Repositorios

Asegúrate de que tus cambios estén en GitHub:

```bash
# Backend
cd BackPs
git add .
git commit -m "Add Render deployment configuration"
git push origin main

# Frontend
cd ../FrontPS/app-oficios
git add .
git commit -m "Add Render deployment configuration"
git push origin main
```

### 2. Deploy de la Base de Datos

1. Ve a [Render Dashboard](https://dashboard.render.com)
2. Click en "New +" → "PostgreSQL"
3. Configura:
   - **Name**: `tuoficio-db`
   - **Database**: `tuoficio`
   - **User**: (auto-generado)
   - **Region**: Elige la más cercana
   - **Plan**: Free
4. Click "Create Database"
5. **Guarda la Internal Database URL** que aparecerá

### 3. Deploy del Backend

#### Opción A: Usando render.yaml (Recomendado)

1. En Render Dashboard, click "New +" → "Blueprint"
2. Conecta tu repositorio `BackPs`
3. Render detectará automáticamente el `render.yaml`
4. Click "Apply"

#### Opción B: Manualmente

1. Click "New +" → "Web Service"
2. Conecta tu repositorio `BackPs`
3. Configura:
   - **Name**: `tuoficio-backend`
   - **Root Directory**: `App`
   - **Environment**: `Java`
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -Dserver.port=$PORT -jar target/App-0.0.1-SNAPSHOT.jar`
   - **Plan**: Free

4. **Variables de Entorno** (click "Advanced" → "Add Environment Variable"):

```
DATABASE_URL=<Internal-Database-URL-from-step-2>
DB_USERNAME=<db-user>
DB_PASSWORD=<db-password>
JAVA_TOOL_OPTIONS=-Xmx512m
JWT_SECRET=<tu-secreto-jwt-seguro>
MERCADOPAGO_ACCESS_TOKEN=<tu-token-mp>
MERCADOPAGO_PUBLIC_KEY=<tu-public-key-mp>
MERCADOPAGO_WEBHOOK_SECRET=<tu-webhook-secret>
EMAIL_USERNAME=tuoficiopracticasupervisada@gmail.com
EMAIL_PASSWORD=<tu-app-password>
STREAM_CHAT_API_KEY=3mjs68wckahw
STREAM_CHAT_API_SECRET=<tu-stream-secret>
FRONTEND_URL=https://tuoficio-frontend.onrender.com
```

5. Click "Create Web Service"

### 4. Deploy del Frontend

1. Click "New +" → "Web Service"
2. Conecta tu repositorio `FrontPS/app-oficios`
3. Configura:
   - **Name**: `tuoficio-frontend`
   - **Root Directory**: `app-oficios`
   - **Environment**: `Docker`
   - **Plan**: Free

4. Click "Create Web Service"

### 5. Configurar CORS en el Backend

Una vez tengas las URLs de Render, actualiza en tu código backend las configuraciones de CORS para permitir peticiones desde el frontend desplegado.

### 6. Configurar Webhooks de MercadoPago

1. Ve a tu cuenta de MercadoPago Developer
2. En la configuración de tu aplicación, agrega la URL del webhook:
   ```
   https://tuoficio-backend.onrender.com/api/v1/pagos/webhook
   ```

## 🔄 Actualizaciones Automáticas

Render redesplegará automáticamente cuando hagas push a tu rama principal:

```bash
git push origin main
```

## 📊 Monitoreo

- **Logs**: Disponibles en el dashboard de cada servicio
- **Métricas**: CPU, memoria y requests en tiempo real
- **Health Checks**: Configurados en `/actuator/health`

## ⚠️ Notas Importantes

1. **Plan Free**: 
   - El servicio se "duerme" después de 15 minutos de inactividad
   - Primera petición después de dormir puede tardar ~30 segundos
   - 750 horas gratis al mes

2. **Base de Datos**:
   - Plan Free expira después de 90 días
   - Backup regularmente tus datos importantes

3. **Variables de Entorno**:
   - NUNCA commitas secretos en el código
   - Usa las variables de entorno de Render

4. **URLs Finales**:
   - Frontend: `https://tuoficio-frontend.onrender.com`
   - Backend: `https://tuoficio-backend.onrender.com`
   - Database: Internal URL only

## 🛠 Troubleshooting

### Backend no inicia
- Verifica los logs en Render Dashboard
- Comprueba que todas las variables de entorno estén configuradas
- Asegúrate de que la DATABASE_URL es correcta

### Frontend muestra errores de API
- Verifica que el backend esté corriendo
- Comprueba la URL en `environment.prod.ts`
- Revisa la configuración de CORS

### Base de datos no conecta
- Verifica que la DATABASE_URL tenga el formato correcto
- Comprueba que el backend y la BD estén en la misma región

## 📞 Soporte

Si tienes problemas:
1. Revisa los logs en Render Dashboard
2. Verifica las variables de entorno
3. Consulta la [documentación de Render](https://render.com/docs)

---

**¡Listo!** Tu aplicación estará disponible en las URLs proporcionadas por Render.
