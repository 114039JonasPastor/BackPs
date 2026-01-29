$ErrorActionPreference = "Stop"

$host_url = "dpg-d5nam0e3jp1c73akdagg-a.oregon-postgres.render.com"
$port = "5432"
$database = "tuoficio_kx40"
$username = "tuoficio_kx40_user"
$password = "0fjX7FnvAzw8M6YaIM2UZueftGQOFB15"

Write-Host "Conectando a la base de datos..." -ForegroundColor Cyan
Write-Host "Host: $host_url" -ForegroundColor Gray
Write-Host "Database: $database" -ForegroundColor Gray
Write-Host ""

# Buscar psql
$psqlPaths = @(
    "C:\Program Files\PostgreSQL\16\bin\psql.exe",
    "C:\Program Files\PostgreSQL\15\bin\psql.exe",
    "C:\Program Files\PostgreSQL\14\bin\psql.exe",
    "C:\Program Files (x86)\PostgreSQL\16\bin\psql.exe",
    "C:\Program Files (x86)\PostgreSQL\15\bin\psql.exe"
)

$psqlExe = $null
foreach ($path in $psqlPaths) {
    if (Test-Path $path) {
        $psqlExe = $path
        break
    }
}

if (-not $psqlExe) {
    Write-Host "ERROR: No se encontró psql.exe en las ubicaciones comunes." -ForegroundColor Red
    Write-Host ""
    Write-Host "Por favor, ejecuta manualmente este SQL en tu cliente de PostgreSQL:" -ForegroundColor Yellow
    Write-Host ""
    Get-Content "c:\Users\jonas\OneDrive\Documentos\GitHub\BackPs\insertar_disponibilidad_todos.sql"
    exit 1
}

Write-Host "Usando psql en: $psqlExe" -ForegroundColor Green

$env:PGPASSWORD = $password

& $psqlExe -h $host_url -p $port -U $username -d $database -f "c:\Users\jonas\OneDrive\Documentos\GitHub\BackPs\insertar_disponibilidad_todos.sql"

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✓ Disponibilidad insertada exitosamente para todos los profesionales" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "✗ Error al ejecutar el script" -ForegroundColor Red
}
