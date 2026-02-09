# ⚡ QUICK START - Fix Email Confirmación

## 🎯 Problema
No se envían emails de confirmación al registrarse en:
**https://tuoficio-frontend.onrender.com/auth/registro**

## 🔧 Solución Rápida (3 pasos)

### 1️⃣ Configurar Render (5 min)
```
1. https://dashboard.render.com
2. tuoficio-backend → Environment
3. Agregar/verificar:
   EMAIL_USERNAME=tuoficiopracticasupervisada@gmail.com
   EMAIL_PASSWORD=twzf lugx xoyd aswk
   BACKEND_URL=https://tuoficio-backend.onrender.com
   FRONTEND_URL=https://tuoficio-frontend.onrender.com
4. Save Changes
```

### 2️⃣ Desplegar Código (5 min)
```bash
cd c:\Users\jonas\OneDrive\Documentos\GitHub\BackPs
git add .
git commit -m "Fix: Email confirmation"
git push origin main
```

### 3️⃣ Verificar (5 min)
```
1. Registrar usuario con email real
2. Revisar bandeja de entrada (y spam)
3. Hacer clic en link de confirmación
4. Verificar que funciona
```

---

## 📊 Cambios Principales

### Backend
- ✅ Login bloqueado para cuentas no verificadas
- ✅ Logging detallado de envío de emails
- ✅ Endpoint de reenvío: `POST /api/v1/registro/resend-confirmation?email={}`
- ✅ URLs dinámicas de producción

### Frontend
- ✅ Mensaje de error específico para cuentas no verificadas
- ✅ Mensaje de éxito detallado con instrucciones
- ✅ Mejor manejo de errores

---

## 🧪 Tests Rápidos

### Test 1: Email llegó
```
1. Registrar en /auth/registro
2. Ver modal "✅ ¡Registro exitoso! Hemos enviado un correo..."
3. Revisar inbox (y spam)
4. Clic en link
5. Ver "✅ ¡Cuenta Confirmada!"
```

### Test 2: Login bloqueado
```
1. Registrar usuario
2. NO confirmar email
3. Intentar login
4. Ver: "⚠️ Cuenta no verificada..."
```

### Test 3: Reenviar email
```powershell
cd c:\Users\jonas\OneDrive\Documentos\GitHub\BackPs
.\resend-confirmation.ps1 -email "test@ejemplo.com"
```

---

## 🚨 Si Falla

### Gmail App Password Inválido
```
1. https://myaccount.google.com/apppasswords
2. Login: tuoficiopracticasupervisada@gmail.com
3. Crear nuevo App Password
4. Actualizar EMAIL_PASSWORD en Render
```

### Logs en Render
```
Dashboard → tuoficio-backend → Logs
Buscar:
  ✅ "Email enviado exitosamente" = OK
  ❌ "ERROR CRÍTICO" = Revisar credenciales
```

---

## 📚 Documentación Completa

- **`SOLUCION_COMPLETA.md`** - Guía detallada completa
- **`FIX_EMAIL_CONFIRMATION.md`** - Pasos de implementación
- **`TROUBLESHOOTING_EMAIL.md`** - Diagnóstico de errores

---

## ✅ Checklist

- [ ] Variables configuradas en Render
- [ ] Código desplegado (git push)
- [ ] Test de registro exitoso
- [ ] Email recibido
- [ ] Link de confirmación funciona
- [ ] Login bloqueado sin confirmación

---

## 📞 Nuevo Endpoint

**Reenviar confirmación:**
```http
POST https://tuoficio-backend.onrender.com/api/v1/registro/resend-confirmation?email=usuario@mail.com
```

**PowerShell:**
```powershell
.\resend-confirmation.ps1 -email "usuario@mail.com"
```

---

**Tiempo total: ~15-20 minutos** ⏱️

**Causa principal**: Variables de entorno faltantes en Render  
**Solución**: Configurar EMAIL_USERNAME y EMAIL_PASSWORD

¡Listo para desplegar! 🚀
