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
rem Leave the rendering client running after the original EXE closes.
start "" /b "%JAVA_HOME%\bin\javaw.exe" -Dsun.java2d.d3d=false -Dclient.background=true -Dweb.bridge.port=8081 -Dweb.bridge.root=WebClient/web -Dclient.cache=../Server/cache -Dclient.host=127.0.0.1 -Dclient.port=43594 -jar build/client.jar 1 local live highmem members english
echo SoloScape browser service started in the background at http://127.0.0.1:8081/
exit /b 0
