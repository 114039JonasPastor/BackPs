# Script para reactivar manualmente un usuario cuando el email no funciona
# Esto es un workaround temporal mientras se arregla la configuración de email

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "🔧 Reactivar Usuario Manualmente" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "⚠️  ADVERTENCIA: Este script modifica directamente la base de datos" -ForegroundColor Yellow
Write-Host "⚠️  Solo úsalo como solución temporal mientras arreglas el email" -ForegroundColor Yellow
Write-Host ""

$email = Read-Host "Ingresa el email del usuario a reactivar"

if (-not $email) {
    Write-Host "❌ Email no proporcionado. Saliendo..." -ForegroundColor Red
    exit
}

Write-Host ""
Write-Host "Generando SQL para reactivar a: $email" -ForegroundColor Cyan
Write-Host ""

$sqlQuery = @"
-- SQL para reactivar usuario: $email
-- Ejecuta esto en tu consola de PostgreSQL (Render o local)

UPDATE auth 
SET active = true 
WHERE mail = '$email';

-- Verificar el cambio
SELECT id, mail, active 
FROM auth 
WHERE mail = '$email';
"@

Write-Host $sqlQuery -ForegroundColor White
Write-Host ""

# Guardar SQL en archivo
$sqlFile = "reactivar_$($email.Replace('@', '_').Replace('.', '_')).sql"
$sqlQuery | Out-File -FilePath $sqlFile -Encoding UTF8

Write-Host "✅ SQL guardado en: $sqlFile" -ForegroundColor Green
Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "📋 Pasos para Ejecutar en Render" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Ve a: https://dashboard.render.com" -ForegroundColor White
Write-Host "2. Selecciona tu base de datos PostgreSQL" -ForegroundColor White
Write-Host "3. Ve a la pestaña 'Shell' o 'Console'" -ForegroundColor White
Write-Host "4. Ejecuta el comando SQL de arriba" -ForegroundColor White
Write-Host ""
Write-Host "O conéctate vía psql:" -ForegroundColor Yellow
Write-Host ""
Write-Host "psql <TU_DATABASE_URL>" -ForegroundColor White
Write-Host ""
Write-Host "Luego ejecuta:" -ForegroundColor Yellow
Write-Host "UPDATE auth SET active = true WHERE mail = '$email';" -ForegroundColor White
Write-Host ""

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "⚠️  Recuerda: Soluciona el Email" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Este es solo un parche temporal. Para solucionar el problema:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Ejecuta: .\test-email-local.ps1" -ForegroundColor White
Write-Host "2. Verifica las variables en Render" -ForegroundColor White
Write-Host "3. Verifica el App Password de Gmail" -ForegroundColor White
Write-Host "4. Revisa los logs de Render cuando alguien se registre" -ForegroundColor White
Write-Host ""
