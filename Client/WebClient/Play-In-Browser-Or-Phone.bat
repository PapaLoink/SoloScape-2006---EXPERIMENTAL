@echo off
setlocal
set "CLIENT_ROOT=%~dp0.."
set "SERVER_ROOT=%~dp0..\..\Server"
set "CLIENT_CACHE_ROOT=%SERVER_ROOT%\cache"
set "WEB_ROOT=%~dp0web"

rem Reuse an existing bridge instead of starting a second game.
powershell.exe -NoProfile -Command "try { $reply=Invoke-WebRequest 'http://127.0.0.1:8081/' -UseBasicParsing -TimeoutSec 2; if ($reply.StatusCode -eq 200 -and $reply.Content -match '/windows-client.js') { exit 0 }; exit 1 } catch { exit 1 }" >nul 2>&1
if not errorlevel 1 (
  start "" "http://127.0.0.1:8081/"
  exit /b 0
)

call "%~dp0..\..\Setup-Java-8.bat"
if errorlevel 1 exit /b 1
if not exist "%CLIENT_ROOT%\Build-Client.bat" (
  echo Error: Client was not found at "%CLIENT_ROOT%".
  exit /b 1
)
if not exist "%CLIENT_CACHE_ROOT%\main_file_cache.dat2" (
  echo Error: server cache not found at "%CLIENT_CACHE_ROOT%".
  exit /b 1
)

echo Building the Java renderer for browser and phone playback...
call "%CLIENT_ROOT%\Build-Client.bat"
if errorlevel 1 exit /b 1

echo.
echo Opening SoloScape in your default browser at http://127.0.0.1:8081/
echo The Java renderer runs with its native game window hidden.
echo Keep this console open while playing in the browser or on your phone.
echo Close this console to stop the renderer and its music.
echo.
pushd "%CLIENT_ROOT%"
"%JAVA_HOME%\bin\java.exe" -Dclient.background=true -Dweb.audio.remote=true -Dweb.bridge.openBrowser=true -Dweb.bridge.strictPort=true -XX:ErrorFile=client-web-hs-err.log -Dweb.bridge.port=8081 -Dweb.bridge.root="%WEB_ROOT%" -Dclient.cache="%CLIENT_CACHE_ROOT%" -Dclient.host=127.0.0.1 -Dclient.port=43594 -jar "build\client.jar" 1 local live highmem members english
set "EXIT_CODE=%ERRORLEVEL%"
popd

echo Browser renderer stopped with code %EXIT_CODE%.
exit /b %EXIT_CODE%
