# Script para probar el registro y diagnostico de email
$backendUrl = "https://tuoficio-backend.onrender.com"

Write-Host "=== TEST DE REGISTRO Y EMAIL ===" -ForegroundColor Cyan
Write-Host ""

# Generar email y datos unicos
$timestamp = Get-Date -Format "yyyyMMddHHmmss"
$testEmail = "test$timestamp@example.com"

Write-Host "1. Probando registro con email: $testEmail" -ForegroundColor Yellow

$body = @{
    nombre = "Test"
    apellido = "Usuario"
    email = $testEmail
    password = "Test123!"
    fechaNacimiento = "1990-01-01"
    telefono = "1234567890"
    documento = "12345678"
    domicilio = @{
        calle = "Test"
        numero = 123
        ciudad = "Test"
        provincia = "Test"
        pais = "Argentina"
    }
} | ConvertTo-Json

try {
    Write-Host "Enviando solicitud de registro..." -ForegroundColor Gray
    $response = Invoke-RestMethod -Uri "$backendUrl/api/v1/registro" -Method Post -Body $body -ContentType "application/json" -ErrorAction Stop
    
    Write-Host "[OK] Registro exitoso!" -ForegroundColor Green
    Write-Host "Respuesta: $($response | ConvertTo-Json)" -ForegroundColor Gray
    
} catch {
    Write-Host "[ERROR] Error en registro:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $errorBody = $reader.ReadToEnd()
        Write-Host "Detalles: $errorBody" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "2. Verifica lo siguiente en Render:" -ForegroundColor Yellow
Write-Host "   - Ve a tu servicio backend en Render" -ForegroundColor Gray
Write-Host "   - Click en Logs en el menu lateral" -ForegroundColor Gray
Write-Host "   - Busca lineas que contengan:" -ForegroundColor Gray
Write-Host "     * EmailService" -ForegroundColor White
Write-Host "     * ERROR" -ForegroundColor White
Write-Host "     * Mail" -ForegroundColor White
Write-Host "     * SMTP" -ForegroundColor White
Write-Host ""
Write-Host "3. Variables criticas a verificar en Render:" -ForegroundColor Yellow
Write-Host "   EMAIL_USERNAME = tuoficiopracticasupervisada@gmail.com" -ForegroundColor Gray
Write-Host "   EMAIL_PASSWORD = twzf lugx xoyd aswk" -ForegroundColor Gray
Write-Host "   SPRING_MAIL_HOST = smtp.gmail.com" -ForegroundColor Gray
Write-Host "   SPRING_MAIL_PORT = 587" -ForegroundColor Gray
Write-Host ""

Read-Host "Presiona Enter para salir"
