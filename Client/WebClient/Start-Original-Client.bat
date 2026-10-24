@echo off
setlocal
rem Opens the original runescape.exe (next to this file or in its Original folder) with a hidden
rem rendering client, and closes that client again when the game window is closed.
rem Start the game server first.
set "EXE=%~dp0runescape.exe"
if not exist "%EXE%" set "EXE=%~dp0Original\runescape.exe"
if not exist "%EXE%" (
    echo runescape.exe was not found in %~dp0 or %~dp0Original
    echo Download it from https://github.com/SoloScape/SoloScape-2006/releases/tag/v1
    echo and save it in one of those folders, then run this file again.
    pause
    exit /b 1
)
call "%~dp0..\..\Setup-Java-8.bat"
if errorlevel 1 (
    pause
    exit /b 1
)
call "%~dp0Enable-Original-Client.bat" >nul
if errorlevel 1 (
    pause
    exit /b 1
)
cd /d "%~dp0.."
if not exist "build\client.jar" (
    call Build-Client.bat
    if errorlevel 1 (
        pause
        exit /b 1
    )
)
rem The watcher runs hidden: it opens the EXE and stops the rendering client when the EXE closes.
start "" powershell.exe -NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File "%~dp0Start-Original-Client.ps1" -Exe "%EXE%"
exit /b 0
