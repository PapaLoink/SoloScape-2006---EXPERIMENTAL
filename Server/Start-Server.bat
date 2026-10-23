@echo off
setlocal
call "%~dp0..\Setup-Java-8.bat"
if errorlevel 1 exit /b 1
cd /d "%~dp0"

if not exist "discord-relay.properties" if exist "discord-relay.properties.example" (
    copy /y "discord-relay.properties.example" "discord-relay.properties" >nul
    echo Created discord-relay.properties from the example file.
    echo Edit that file to enable Discord level-up and boss-kill announcements.
    echo.
)

if not defined PRS_AUDIT443 set "PRS_AUDIT443=true"

rem Promote the fixed revision-443 server after the previous process releases server.jar.
if exist "dist\server-totallevelfix.jar" move /y "dist\server-totallevelfix.jar" "dist\server.jar" >nul
if exist "dist\server-regionfix.jar" move /y "dist\server-regionfix.jar" "dist\server.jar" >nul

where java >nul 2>&1
if errorlevel 1 (
    echo ERROR: java was not found. Install Java and add its bin folder to PATH.
    pause
    exit /b 1
)

if not exist "dist\server.jar" (
    echo ERROR: dist\server.jar was not found. Run Build-Server.bat first.
    pause
    exit /b 1
)

echo Revision 443 audit: %PRS_AUDIT443%
set "AUTO_START=false"
if /I "%~1"=="--auto-start" set "AUTO_START=true"
"%JAVA_HOME%\bin\java.exe" -Xmx1024m -Dprs.audit443=%PRS_AUDIT443% -Dprs.traceGameplay=true -Dprs.exitOnServerShutdown=true -Dprs.dev.autoStart=%AUTO_START% -jar "dist\server.jar"
set "EXIT_CODE=%ERRORLEVEL%"
if not "%EXIT_CODE%"=="0" (
    echo.
    echo Server exited with code %EXIT_CODE%.
    pause
)
exit /b %EXIT_CODE%
