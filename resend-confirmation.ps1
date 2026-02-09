# Script para reenviar email de confirmación
# Uso: .\resend-confirmation.ps1 -email "usuario@ejemplo.com"

param(
    [Parameter(Mandatory=$true)]
    [string]$email,
    
    [Parameter(Mandatory=$false)]
    [string]$backendUrl = "https://tuoficio-backend.onrender.com"
)

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "   REENVIAR EMAIL DE CONFIRMACIÓN" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Backend URL: $backendUrl" -ForegroundColor Yellow
Write-Host "Email: $email" -ForegroundColor Yellow
Write-Host ""

$endpoint = "$backendUrl/api/v1/registro/resend-confirmation?email=$email"

Write-Host "Enviando solicitud..." -ForegroundColor Yellow

try {
    $response = Invoke-RestMethod -Uri $endpoint -Method POST -ErrorAction Stop
    Write-Host ""
    Write-Host "✅ ÉXITO" -ForegroundColor Green
    Write-Host $response -ForegroundColor Green
    Write-Host ""
    Write-Host "Revisa la bandeja de entrada del correo: $email" -ForegroundColor Cyan
} catch {
    Write-Host ""
    Write-Host "❌ ERROR" -ForegroundColor Red
    Write-Host "Status Code: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "Mensaje: $($_.Exception.Message)" -ForegroundColor Red
    
    # Try to parse error response
    try {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $errorDetails = $reader.ReadToEnd()
        $reader.Close()
        Write-Host "Detalles:" -ForegroundColor Red
        Write-Host $errorDetails -ForegroundColor Red
    } catch {
        # Ignore parsing errors
    }
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
