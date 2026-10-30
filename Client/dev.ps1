param(
    [switch]$PrepareOnly,
    [ValidateRange(1, 65535)]
    [int]$McpPort = 7780,
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$ClientArgs
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $Root

$SourceRoot = Join-Path $Root "src"
$BuildRoot = Join-Path $Root "build"
$ClassesDir = Join-Path $BuildRoot "classes"
$AgentClassesDir = Join-Path $BuildRoot "hotswap-agent-classes"
$AgentJar = Join-Path $BuildRoot "hotswap-agent.jar"
$RestartFlag = Join-Path $BuildRoot "hotswap-restart.flag"
$SourcesFile = Join-Path $BuildRoot "dev-sources.txt"
$AgentSource = Join-Path $Root "dev\hotswap\HotSwapAgent.java"
$AgentManifest = Join-Path $Root "dev\hotswap\MANIFEST.MF"
$ServerRoot = Join-Path (Split-Path $Root -Parent) "Server"
$ServerCache = Join-Path $ServerRoot "cache"
$LibJars = (Get-ChildItem -Path (Join-Path $Root "lib") -Filter *.jar -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName }) -join ";"
$CompileCp = if ($LibJars) { "$ClassesDir;$LibJars" } else { $ClassesDir }

function Assert-Command([string]$Name) {
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "$Name was not found on PATH."
    }
}

function Compile-AllClientClasses {
    Write-Host "[Dev] Initial compile..." -ForegroundColor Cyan
    New-Item -ItemType Directory -Force -Path $ClassesDir | Out-Null
    New-Item -ItemType Directory -Force -Path $BuildRoot | Out-Null

    $sources = Get-ChildItem -Path $SourceRoot -Recurse -Filter *.java | Sort-Object FullName
    $sourceLines = foreach ($source in $sources) {
        '"' + ($source.FullName -replace '\\', '/') + '"'
    }
    Set-Content -Path $SourcesFile -Value $sourceLines -Encoding ASCII

    & javac -encoding UTF-8 -cp $CompileCp -d $ClassesDir "@$SourcesFile"
    if ($LASTEXITCODE -ne 0) {
        throw "Initial client compilation failed with exit code $LASTEXITCODE."
    }
    Copy-Item -LiteralPath (Join-Path $SourceRoot 'assets') -Destination $ClassesDir -Recurse -Force
}

function Build-HotSwapAgent {
    Write-Host "[Dev] Building HotSwap agent..." -ForegroundColor Cyan
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

function Assert-ServerCache {
    $cacheFile = Join-Path $ServerCache "main_file_cache.dat2"
    $fallbackSource = Join-Path $ServerCache "map-fallback"
    if (-not (Test-Path $cacheFile)) {
        throw "Server cache was not found at $ServerCache. Expected Server\cache next to Client."
    }
    if (-not (Test-Path $fallbackSource)) {
        throw "Map fallbacks were not found at $fallbackSource"
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

    $javacArgs = @(
        "-encoding", "UTF-8",
        "-cp", $CompileCp,
        "-sourcepath", $SourceRoot,
        "-d", $ClassesDir
    ) + $Paths

    & javac @javacArgs
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[Dev] Compile failed; running client was left unchanged." -ForegroundColor Red
        return $false
    }

    Write-Host "[Dev] Compile complete. Waiting for JVM HotSwap..." -ForegroundColor Green
    return $true
}

function Start-DevClient {
    if (Test-Path $RestartFlag) {
        Remove-Item -Force $RestartFlag
    }

    $argsToUse = $ClientArgs
    if (-not $argsToUse -or $argsToUse.Count -eq 0) {
        $argsToUse = @("1", "local", "live", "highmem", "members", "english")
    }

    $errorLog = Join-Path $Root "client-hs-err.log"
    $agentArgument = "-javaagent:$AgentJar=$ClassesDir;$RestartFlag"
    $cacheArgument = '-Dclient.cache="{0}"' -f $ServerCache
    $javaArgs = @(
        "-Dsun.java2d.d3d=false",
        "-XX:ErrorFile=$errorLog",
        $cacheArgument,
        "-Dsoloscape.mcp.port=$McpPort",
        "-Dclient.host=127.0.0.1",
        "-Dclient.port=43594",
        $agentArgument,
        "-cp", $CompileCp,
        "unpackaged.Client"
    ) + $argsToUse

    Write-Host "[Dev] Starting client with live HotSwap..." -ForegroundColor Cyan
    Write-Host "[Dev] Developer tools dashboard: http://127.0.0.1:$McpPort/" -ForegroundColor Cyan
    Write-Host "[Dev] MCP endpoint: http://127.0.0.1:$McpPort/mcp" -ForegroundColor Cyan
    Write-Host "[Dev] Save a .java file under src\ to apply changes." -ForegroundColor DarkGray
    return Start-Process -FilePath "java" -ArgumentList $javaArgs -WorkingDirectory $Root -PassThru -NoNewWindow
}

function Read-RestartReason {
    if (-not (Test-Path $RestartFlag)) {
        return $null
    }
    try {
        return (Get-Content -Path $RestartFlag -Raw).Trim()
    } catch {
        return "Structural class change"
    }
}

Assert-Command "java"
Assert-Command "javac"
Assert-Command "jar"

Compile-AllClientClasses
Build-HotSwapAgent

if ($PrepareOnly) {
    Write-Host "[Dev] HotSwap development build is ready." -ForegroundColor Green
    Write-Host "[Dev] Run Start-Client-Dev-Mode.bat to launch the watcher." -ForegroundColor Green
    exit 0
}

Assert-ServerCache
$sourceTimes = New-SourceSnapshot
$client = $null

try {
    $client = Start-DevClient

    while ($true) {
        Start-Sleep -Milliseconds 250

        $reason = Read-RestartReason
        if ($reason) {
            Write-Host "[Dev] HotSwap cannot apply this structural edit." -ForegroundColor Yellow
            Write-Host "[Dev] Auto-restarting client: $reason" -ForegroundColor Yellow
            if ($client -and -not $client.HasExited) {
                Stop-Process -Id $client.Id -Force -ErrorAction SilentlyContinue
                $client.WaitForExit()
            }
            Remove-Item -Force $RestartFlag -ErrorAction SilentlyContinue
            $client = Start-DevClient
            continue
        }

        if ($client.HasExited) {
            Write-Host "[Dev] Client closed. Dev watcher stopped." -ForegroundColor DarkGray
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

        foreach ($knownPath in @($sourceTimes.Keys)) {
            if (-not $currentPaths.ContainsKey($knownPath)) {
                $sourceTimes.Remove($knownPath)
                Write-Host "[Dev] Source removed: $knownPath. Restart may be required." -ForegroundColor Yellow
            }
        }

        if ($changed.Count -gt 0) {
            # Give editors a moment to finish atomic-save/rename sequences.
            Start-Sleep -Milliseconds 125
            [void](Compile-ChangedSources $changed.ToArray())
        }
    }
} finally {
    if ($client -and -not $client.HasExited) {
        Stop-Process -Id $client.Id -Force -ErrorAction SilentlyContinue
    }
}
