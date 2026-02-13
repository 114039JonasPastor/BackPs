# Script PowerShell para registrar un administrador
# 
# ⚠️ IMPORTANTE: Reemplaza la URL con tu URL real de Render
# Ejemplo: https://tuoficio-backend.onrender.com
# Para obtener tu URL, ve a: https://dashboard.render.com → Tu servicio → Settings

# Configuración
$baseUrl = "https://tuoficio-backend.onrender.com"  # ⬅️ REEMPLAZA CON TU URL DE RENDER
$endpoint = "$baseUrl/api/v1/registro/administrador"

# Datos del administrador
$adminData = @{
    name = "Admin"
    lastName = "Sistema"
    mail = "admin@tuoficio.com"
    password = "Admin123!"
    idTipoDoc = 1
    documento = "12345678"
    telefono = "3512345678"
    nacimiento = "1990-01-01"
    idBarrio = 1
    calle = "Av. Colón"
    numero = "100"
    piso = $null
    depto = $null
    observaciones = $null
} | ConvertTo-Json

Write-Host "🔵 Registrando administrador..." -ForegroundColor Cyan
Write-Host "URL: $endpoint" -ForegroundColor Gray
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri $endpoint -Method Post -Body $adminData -ContentType "application/json"
    
    Write-Host "✅ Administrador registrado exitosamente!" -ForegroundColor Green
    Write-Host ""
    Write-Host "📋 Respuesta:" -ForegroundColor Yellow
    $response | ConvertTo-Json -Depth 10
    Write-Host ""
    Write-Host "🔑 Token JWT:" -ForegroundColor Cyan
    Write-Host $response.token -ForegroundColor White
    
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    $errorMessage = $_.ErrorDetails.Message
    
    Write-Host "❌ Error al registrar administrador" -ForegroundColor Red
    Write-Host "Status Code: $statusCode" -ForegroundColor Yellow
    Write-Host "Error: $errorMessage" -ForegroundColor Red
    
    if ($errorMessage) {
        try {
            $errorJson = $errorMessage | ConvertFrom-Json
            Write-Host ""
            Write-Host "Detalles del error:" -ForegroundColor Yellow
            $errorJson | Format-List
        } catch {
            Write-Host $errorMessage -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "💡 Tip: Ahora puedes iniciar sesión con:" -ForegroundColor Cyan
Write-Host "   Email: admin@tuoficio.com" -ForegroundColor White
Write-Host "   Password: Admin123!" -ForegroundColor White
