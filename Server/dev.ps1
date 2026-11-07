param(
    [switch]$PrepareOnly
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $Root

$SourceRoot = Join-Path $Root "src\main\java"
$LibDir = Join-Path $Root "lib"
$BuildRoot = Join-Path $Root "build"
$ClassesDir = Join-Path $BuildRoot "classes"
$AgentClassesDir = Join-Path $BuildRoot "hotswap-agent-classes"
$AgentJar = Join-Path $BuildRoot "hotswap-agent.jar"
$RestartFlag = Join-Path $BuildRoot "hotswap-restart.flag"
$SourcesFile = Join-Path $BuildRoot "dev-sources.txt"
$AgentSource = Join-Path $Root "dev\hotswap\HotSwapAgent.java"
$AgentManifest = Join-Path $Root "dev\hotswap\MANIFEST.MF"

if (-not $env:PRS_AUDIT443) {
    $env:PRS_AUDIT443 = "true"
}

function Assert-Command([string]$Name) {
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "$Name was not found on PATH."
    }
}

function Compile-AllServerClasses {
    Write-Host "[Dev] Initial server compile..." -ForegroundColor Cyan
    New-Item -ItemType Directory -Force -Path $BuildRoot | Out-Null
    if (Test-Path $ClassesDir) {
        Remove-Item -Recurse -Force $ClassesDir
    }
    New-Item -ItemType Directory -Force -Path $ClassesDir | Out-Null

    $sources = Get-ChildItem -Path $SourceRoot -Recurse -Filter *.java | Sort-Object FullName
    $sourceLines = foreach ($source in $sources) {
        '"' + ($source.FullName -replace '\\', '/') + '"'
    }
    Set-Content -Path $SourcesFile -Value $sourceLines -Encoding ASCII

    $libraryClasspath = Join-Path $LibDir "*"
    & javac -encoding UTF-8 -Xmaxerrs 5000 -classpath $libraryClasspath -d $ClassesDir "@$SourcesFile"
    if ($LASTEXITCODE -ne 0) {
        throw "Initial server compilation failed with exit code $LASTEXITCODE."
    }
}

function Build-HotSwapAgent {
    Write-Host "[Dev] Building server HotSwap agent..." -ForegroundColor Cyan
    if (Test-Path $AgentClassesDir) {
        Remove-Item -Recurse -Force $AgentClassesDir
    }
    New-Item -ItemType Directory -Force -Path $AgentClassesDir | Out-Null

    & javac -encoding UTF-8 -d $AgentClassesDir $AgentSource
    if ($LASTEXITCODE -ne 0) {
        throw "HotSwap agent compilation failed with exit code $LASTEXITCODE."
    }

    & jar cfm $AgentJar $AgentManifest -C $AgentClassesDir .
    if ($LASTEXITCODE -ne 0) {
        throw "HotSwap agent packaging failed with exit code $LASTEXITCODE."
    }
}

function New-SourceSnapshot {
    $snapshot = @{}
    Get-ChildItem -Path $SourceRoot -Recurse -Filter *.java | ForEach-Object {
        $snapshot[$_.FullName] = $_.LastWriteTimeUtc.Ticks
    }
    return $snapshot
}

function Compile-ChangedSources([string[]]$Paths) {
    if (-not $Paths -or $Paths.Count -eq 0) {
        return $true
    }

    $display = ($Paths | ForEach-Object { Split-Path $_ -Leaf }) -join ", "
    Write-Host "[Dev] Compiling: $display" -ForegroundColor Yellow

    $compileClasspath = "$ClassesDir;$LibDir\*"
    $javacArgs = @(
        "-encoding", "UTF-8",
        "-Xmaxerrs", "5000",
        "-classpath", $compileClasspath,
        "-sourcepath", $SourceRoot,
        "-d", $ClassesDir
    ) + $Paths

    & javac @javacArgs
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[Dev] Compile failed; the running server keeps its previous loaded classes." -ForegroundColor Red
        return $false
    }

    Write-Host "[Dev] Compile complete. Waiting for JVM HotSwap..." -ForegroundColor Green
    return $true
}

function Start-DevServer {
    if (Test-Path $RestartFlag) {
        Remove-Item -Force $RestartFlag
    }

    $javaClasspath = "$ClassesDir;$LibDir\*"
    $agentArgument = "-javaagent:$AgentJar=$ClassesDir;$RestartFlag"
    $javaArgs = @(
        "-Xmx1024m",
        "-Dprs.audit443=$($env:PRS_AUDIT443)",
        "-Dprs.traceGameplay=true",
        "-Dprs.exitOnServerShutdown=true",
        "-Dprs.dev.hotReload=true",
        "-Dprs.dev.autoStart=true",
        "-Dprs.dev.restartFlag=$RestartFlag",
        $agentArgument,
        "-cp", $javaClasspath,
        "com.rs2.launcher.ControlPanel"
    )
    # Start-Process joins arguments with spaces, so quote any that contain one (e.g. a folder named "AI Projects").
    $javaArgs = $javaArgs | ForEach-Object { if ($_ -match ' ') { '"' + $_ + '"' } else { $_ } }

    Write-Host "[Dev] Starting server with live HotSwap..." -ForegroundColor Cyan
    Write-Host "[Dev] Save a .java file under src\main\java to apply changes." -ForegroundColor DarkGray
    return Start-Process -FilePath "java" -ArgumentList $javaArgs -WorkingDirectory $Root -PassThru -NoNewWindow
}

function Read-RestartReason {
    if (-not (Test-Path $RestartFlag)) {
        return $null
    }
    try {
        return (Get-Content -Path $RestartFlag -Raw).Trim()
    }
    catch {
        return "Structural class change"
    }
}

function Restart-DevServer([System.Diagnostics.Process]$OldServer, [bool]$FullRecompile) {
    if ($OldServer -and -not $OldServer.HasExited) {
        Write-Host "[Dev] Waiting for graceful server shutdown..." -ForegroundColor Yellow
        $deadline = [DateTime]::UtcNow.AddSeconds(20)
        while (-not $OldServer.HasExited -and [DateTime]::UtcNow -lt $deadline) {
            Start-Sleep -Milliseconds 250
        }
        if (-not $OldServer.HasExited) {
            Write-Host "[Dev] Graceful shutdown timed out; forcing the dev JVM to stop." -ForegroundColor Red
            Stop-Process -Id $OldServer.Id -Force -ErrorAction SilentlyContinue
            $OldServer.WaitForExit()
        }
    }

    Remove-Item -Force $RestartFlag -ErrorAction SilentlyContinue

    if ($FullRecompile) {
        Compile-AllServerClasses
    }

    return Start-DevServer
}

Assert-Command "java"
Assert-Command "javac"
Assert-Command "jar"

Compile-AllServerClasses
Build-HotSwapAgent

if ($PrepareOnly) {
    Write-Host "[Dev] Server HotSwap development build is ready." -ForegroundColor Green
    Write-Host "[Dev] Run Start-Server-Dev-Mode.bat to launch the live server watcher." -ForegroundColor Green
    exit 0
}

$sourceTimes = New-SourceSnapshot
$server = $null
$restartPending = $false
$restartReason = $null
$restartRequestedAt = $null
$needsFullRecompile = $false

try {
    $server = Start-DevServer

    while ($true) {
        Start-Sleep -Milliseconds 250

        $reason = Read-RestartReason
        if ($reason -and -not $restartPending) {
            $restartPending = $true
            $restartReason = $reason
            $restartRequestedAt = [DateTime]::UtcNow
            Write-Host "[Dev] HotSwap cannot apply this structural edit." -ForegroundColor Yellow
            Write-Host "[Dev] Graceful auto-restart requested: $restartReason" -ForegroundColor Yellow
        }

        if ($restartPending) {
            if ($server.HasExited) {
                Remove-Item -Force $RestartFlag -ErrorAction SilentlyContinue
                if ($needsFullRecompile) {
                    Compile-AllServerClasses
                    $needsFullRecompile = $false
                }
                $sourceTimes = New-SourceSnapshot
                $server = Start-DevServer
                $restartPending = $false
                $restartReason = $null
                $restartRequestedAt = $null
                continue
            }

            if ($restartRequestedAt -and (([DateTime]::UtcNow - $restartRequestedAt).TotalSeconds -gt 20)) {
                Write-Host "[Dev] Graceful shutdown timed out; forcing the dev JVM to stop." -ForegroundColor Red
                Stop-Process -Id $server.Id -Force -ErrorAction SilentlyContinue
                $server.WaitForExit()
                Remove-Item -Force $RestartFlag -ErrorAction SilentlyContinue
                if ($needsFullRecompile) {
                    Compile-AllServerClasses
                    $needsFullRecompile = $false
                }
                $sourceTimes = New-SourceSnapshot
                $server = Start-DevServer
                $restartPending = $false
                $restartReason = $null
                $restartRequestedAt = $null
            }
            continue
        }

        if ($server.HasExited) {
            Write-Host "[Dev] Server closed. Dev watcher stopped." -ForegroundColor DarkGray
            break
        }

        $changed = New-Object System.Collections.Generic.List[string]
        $currentPaths = @{}

        Get-ChildItem -Path $SourceRoot -Recurse -Filter *.java | ForEach-Object {
            $path = $_.FullName
            $ticks = $_.LastWriteTimeUtc.Ticks
            $currentPaths[$path] = $true

            if (-not $sourceTimes.ContainsKey($path) -or $sourceTimes[$path] -ne $ticks) {
                $sourceTimes[$path] = $ticks
                $changed.Add($path)
            }
        }

        $removed = New-Object System.Collections.Generic.List[string]
        foreach ($knownPath in @($sourceTimes.Keys)) {
            if (-not $currentPaths.ContainsKey($knownPath)) {
                $sourceTimes.Remove($knownPath)
                $removed.Add($knownPath)
            }
        }

        if ($removed.Count -gt 0) {
            $needsFullRecompile = $true
            $removedDisplay = ($removed | ForEach-Object { Split-Path $_ -Leaf }) -join ", "
            Set-Content -Path $RestartFlag -Value ("Source removed: " + $removedDisplay) -Encoding ASCII
            continue
        }

        if ($changed.Count -gt 0) {
            # Give editors a moment to finish atomic-save/rename sequences.
            Start-Sleep -Milliseconds 125
            [void](Compile-ChangedSources $changed.ToArray())
        }
    }
}
finally {
    if ($server -and -not $server.HasExited) {
        Stop-Process -Id $server.Id -Force -ErrorAction SilentlyContinue
    }
}
