# Script para verificar que tu backend en Render está funcionando

# ⬅️ REEMPLAZA CON TU URL DE RENDER
$baseUrl = "https://tuoficio-backend.onrender.com"

Write-Host "🔍 Verificando conexión con backend..." -ForegroundColor Cyan
Write-Host "URL: $baseUrl" -ForegroundColor Gray
Write-Host ""

# Test 1: Health check
Write-Host "1️⃣ Verificando estado del servidor (health)..." -ForegroundColor Yellow
try {
    $healthResponse = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method Get -TimeoutSec 10
    Write-Host "   ✅ Servidor está UP" -ForegroundColor Green
    $healthResponse | ConvertTo-Json
} catch {
    Write-Host "   ❌ No se pudo conectar al health endpoint" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Test 2: API base
Write-Host "2️⃣ Verificando API base..." -ForegroundColor Yellow
try {
    $apiResponse = Invoke-RestMethod -Uri "$baseUrl/api/v1" -Method Get -TimeoutSec 10 -ErrorAction SilentlyContinue
    Write-Host "   ✅ API responde correctamente" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 404) {
        Write-Host "   ⚠️ API existe pero ruta /api/v1 no está configurada (esto es normal)" -ForegroundColor Yellow
    } else {
        Write-Host "   ❌ Error al acceder a la API" -ForegroundColor Red
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""

# Test 3: Verificar si existe rol ADMINISTRADOR en BD
Write-Host "3️⃣ Verificando endpoint de registro de administrador..." -ForegroundColor Yellow
try {
    # Intentamos hacer un POST sin datos para ver si el endpoint existe
    $headers = @{
        "Content-Type" = "application/json"
    }
    $response = Invoke-WebRequest -Uri "$baseUrl/api/v1/registro/administrador" -Method Post -Headers $headers -Body "{}" -ErrorAction Stop
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 400 -or $statusCode -eq 500) {
        Write-Host "   ✅ Endpoint existe (responde con error esperado por falta de datos)" -ForegroundColor Green
    } else {
        Write-Host "   ⚠️ Status Code: $statusCode" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host ""

# Verificar que existen los roles en la BD
Write-Host "📋 CHECKLIST antes de crear administrador:" -ForegroundColor Cyan
Write-Host "   □ La base de datos está creada y conectada" -ForegroundColor White
Write-Host "   □ Las tablas fueron creadas (ejecutaste ps.sql)" -ForegroundColor White
Write-Host "   □ Los roles ADMINISTRADOR, CLIENTE, PROFESIONAL existen:" -ForegroundColor White
Write-Host "     INSERT INTO roles (descripcion) VALUES ('ADMINISTRADOR'), ('PROFESIONAL'), ('CLIENTE');" -ForegroundColor Gray
Write-Host "   □ Existe al menos un tipo de documento (idTipoDoc = 1)" -ForegroundColor White
Write-Host "   □ Existe al menos un barrio (idBarrio = 1)" -ForegroundColor White
Write-Host ""
Write-Host "💡 Si todo está OK, ejecuta: .\test-admin.ps1" -ForegroundColor Green
