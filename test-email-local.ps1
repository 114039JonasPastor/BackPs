# Script para probar el envío de emails localmente antes de desplegar

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "🔍 Test de Configuración de Email" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Verificar variables de entorno
$emailUsername = $env:EMAIL_USERNAME
$emailPassword = $env:EMAIL_PASSWORD

if (-not $emailUsername) {
    $emailUsername = "tuoficiopracticasupervisada@gmail.com"
    Write-Host "⚠️  EMAIL_USERNAME no está configurada, usando valor por defecto" -ForegroundColor Yellow
} else {
    Write-Host "✅ EMAIL_USERNAME: $emailUsername" -ForegroundColor Green
}

if (-not $emailPassword) {
    $emailPassword = "twzf lugx xoyd aswk"
    Write-Host "⚠️  EMAIL_PASSWORD no está configurada, usando valor por defecto" -ForegroundColor Yellow
} else {
    Write-Host "✅ EMAIL_PASSWORD: ****" -ForegroundColor Green
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "📧 Instrucciones para Reenviar Email" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1️⃣  Si un usuario NO recibió el email de confirmación, usa este comando:" -ForegroundColor White
Write-Host ""

# Solicitar el email del usuario
$userEmail = Read-Host "Ingresa el email del usuario que necesita reenvío"

if ($userEmail) {
    Write-Host ""
    Write-Host "Comando para PRODUCCIÓN (Render):" -ForegroundColor Yellow
    Write-Host "curl -X POST 'https://tuoficio-backend.onrender.com/api/v1/registro/resend-confirmation?email=$userEmail'" -ForegroundColor White
    Write-Host ""
    
    Write-Host "Comando para LOCAL:" -ForegroundColor Yellow
    Write-Host "curl -X POST 'http://localhost:8081/api/v1/registro/resend-confirmation?email=$userEmail'" -ForegroundColor White
    Write-Host ""
    
    $testLocal = Read-Host "¿Quieres probarlo en LOCAL ahora? (s/n)"
    
    if ($testLocal -eq "s") {
        Write-Host ""
        Write-Host "🚀 Enviando petición al servidor local..." -ForegroundColor Cyan
        
        try {
            $response = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/registro/resend-confirmation?email=$userEmail" -Method Post
            Write-Host "✅ Respuesta exitosa: $response" -ForegroundColor Green
        } catch {
            Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
            Write-Host ""
            Write-Host "Asegúrate de que:" -ForegroundColor Yellow
            Write-Host "1. El servidor backend está corriendo en localhost:8081" -ForegroundColor White
            Write-Host "2. El email existe en la base de datos" -ForegroundColor White
            Write-Host "3. Las variables EMAIL_USERNAME y EMAIL_PASSWORD están configuradas" -ForegroundColor White
        }
    }
    
    $testProd = Read-Host "¿Quieres probarlo en PRODUCCIÓN ahora? (s/n)"
    
    if ($testProd -eq "s") {
        Write-Host ""
        Write-Host "🚀 Enviando petición al servidor de producción..." -ForegroundColor Cyan
        
        try {
            $response = Invoke-RestMethod -Uri "https://tuoficio-backend.onrender.com/api/v1/registro/resend-confirmation?email=$userEmail" -Method Post
            Write-Host "✅ Respuesta exitosa: $response" -ForegroundColor Green
        } catch {
            Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
            if ($_.Exception.Response) {
                $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
                $responseBody = $reader.ReadToEnd()
                Write-Host "📋 Detalle del error: $responseBody" -ForegroundColor Yellow
            }
        }
    }
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "🔧 Verificar Variables en Render" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Ve a: https://dashboard.render.com" -ForegroundColor White
Write-Host "Selecciona el servicio: tuoficio-backend" -ForegroundColor White
Write-Host "Ve a la pestaña: Environment" -ForegroundColor White
Write-Host ""
Write-Host "Verifica que estas variables estén configuradas:" -ForegroundColor Yellow
Write-Host "  EMAIL_USERNAME = tuoficiopracticasupervisada@gmail.com" -ForegroundColor White
Write-Host "  EMAIL_PASSWORD = twzf lugx xoyd aswk" -ForegroundColor White
Write-Host "  BACKEND_URL = https://tuoficio-backend.onrender.com" -ForegroundColor White
Write-Host "  FRONTEND_URL = https://tuoficio-frontend.onrender.com" -ForegroundColor White
Write-Host ""
Write-Host "⚠️  Si añades o cambias variables, debes hacer REDEPLOY manual" -ForegroundColor Yellow
Write-Host ""

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "🔐 Verificar Gmail App Password" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Ve a: https://myaccount.google.com/apppasswords" -ForegroundColor White
Write-Host "2. Login con: tuoficiopracticasupervisada@gmail.com" -ForegroundColor White
Write-Host "3. Verifica que el App Password existe o crea uno nuevo" -ForegroundColor White
Write-Host "4. Si creas uno nuevo, actualiza EMAIL_PASSWORD en Render" -ForegroundColor White
Write-Host ""

Write-Host "✅ Script completado" -ForegroundColor Green
