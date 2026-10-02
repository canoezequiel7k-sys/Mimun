$ErrorActionPreference = "Stop"

if (-not $env:DB_BACKUP_URL) {
    throw "Set DB_BACKUP_URL to the native PostgreSQL connection URL first."
}

$pgDump = Get-Command pg_dump -ErrorAction SilentlyContinue
if (-not $pgDump) {
    throw "Install PostgreSQL client tools so pg_dump is available on PATH."
}

$backupDirectory = Join-Path $HOME "mimun-backups"
New-Item -ItemType Directory -Force -Path $backupDirectory | Out-Null
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$backupPath = Join-Path $backupDirectory "mimun-$timestamp.dump"

& $pgDump.Source --dbname $env:DB_BACKUP_URL --format custom --no-owner --no-acl --file $backupPath
if ($LASTEXITCODE -ne 0) {
    throw "pg_dump failed with exit code $LASTEXITCODE."
}

Write-Output "Backup created: $backupPath"