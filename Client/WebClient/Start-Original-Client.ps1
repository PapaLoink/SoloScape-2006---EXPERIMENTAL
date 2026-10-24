# Starts the hidden rendering client for the original runescape.exe, opens the EXE, and stops the
# rendering client again when the EXE is closed. Called by Start-Original-Client.bat.
param([string]$Exe)
$ErrorActionPreference = 'Stop'

$webClient = $PSScriptRoot
$clientDir = Split-Path $webClient -Parent
if (-not $Exe) { $Exe = Join-Path $webClient 'runescape.exe' }

function Test-Service {
    try {
        $reply = Invoke-WebRequest 'http://127.0.0.1:8081/' -UseBasicParsing -TimeoutSec 2
        return $reply.StatusCode -eq 200 -and $reply.Content -match '/windows-client.js'
    } catch {
        return $false
    }
}

# A rendering client left over from an earlier session can be stuck on a black screen; always start fresh.
foreach ($old in @(Get-CimInstance Win32_Process -Filter "Name='javaw.exe'" |
        Where-Object { $_.CommandLine -match 'web\.bridge\.port=8081' })) {
    Stop-Process -Id $old.ProcessId -Force -ErrorAction SilentlyContinue
    # Until it has exited it still answers on port 8081, which would look like the new client is ready.
    Wait-Process -Id $old.ProcessId -Timeout 15 -ErrorAction SilentlyContinue
}

$service = Start-Process -FilePath (Join-Path $env:JAVA_HOME 'bin\javaw.exe') -WorkingDirectory $clientDir -PassThru -ArgumentList @(
    '-Dclient.background=true', '-Dweb.bridge.port=8081', '-Dweb.bridge.root=WebClient/web',
    '-Dclient.cache=../Server/cache', '-Dclient.host=127.0.0.1', '-Dclient.port=43594',
    '-jar', 'build/client.jar', '1', 'local', 'live', 'highmem', 'members', 'english')

try {
    # The EXE shows an error page if it opens before the page is served.
    for ($i = 0; -not (Test-Service); $i++) {
        if ($service.HasExited) { throw 'The rendering client stopped while starting.' }
        if ($i -ge 120) { throw 'The rendering client did not start within 60 seconds.' }
        Start-Sleep -Milliseconds 500
    }
    $game = Start-Process -FilePath $Exe -WorkingDirectory (Split-Path $Exe -Parent) -PassThru
    $game.WaitForExit()
} finally {
    if (-not $service.HasExited) { Stop-Process -Id $service.Id -Force -ErrorAction SilentlyContinue }
}
