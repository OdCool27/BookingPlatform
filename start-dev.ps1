param(
    [string]$TrustedOrigins = 'http://localhost:5173,http://127.0.0.1:5173,http://localhost:4173,http://127.0.0.1:4173'
)

$ErrorActionPreference = 'Stop'

# This launcher opts into local browser origins only for development. Production must
# use its own explicit configuration rather than accidentally inheriting these origins.
if ($env:APP_ENV -and $env:APP_ENV -ne 'dev') {
    throw 'start-dev.ps1 requires APP_ENV=dev (or unset). Use your normal launcher for test/prod.'
}

Push-Location $PSScriptRoot
try {
    # Environment variables are inherited by the Java process launched below. Setting
    # them in another terminal cannot update a backend that is already running.
    $env:APP_ENV = 'dev'
    $env:TRUSTED_FRONTEND_ORIGINS = $TrustedOrigins
    Write-Host "Development frontend origins: $TrustedOrigins"
    Write-Host 'Stop any existing backend on the configured port before starting this launcher.'

    # Build the current source so a stale JAR does not hide routing/configuration changes.
    & mvn.cmd -q -DskipTests package
    if ($LASTEXITCODE -ne 0) { throw 'Backend build failed.' }
    & java --enable-native-access=ALL-UNNAMED -jar target/OsyntraBookingPlatform-1.0-SNAPSHOT.jar
    if ($LASTEXITCODE -ne 0) { throw 'Backend exited with an error.' }
} finally {
    Pop-Location
}
