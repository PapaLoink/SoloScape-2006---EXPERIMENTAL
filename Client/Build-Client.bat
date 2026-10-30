@echo off
setlocal EnableDelayedExpansion
call "%~dp0..\Setup-Java-8.bat"
if errorlevel 1 exit /b 1
cd /d "%~dp0"

if not exist "Play-Client.bat.template" (
    echo ERROR: Play-Client.bat.template is missing. Restore it from the project files.
    exit /b 1
)

where javac >nul 2>&1
if errorlevel 1 (
    echo Error: javac was not found. Install a JDK and add its bin directory to PATH.
    exit /b 1
)

if not exist "build\classes" mkdir "build\classes"

echo Collecting Java sources...
>"build\sources.txt" (
    for /r "src" %%F in (*.java) do (
        set "source=%%F"
        echo "!source:\=/!"
    )
)

echo Compiling client...
"%JAVA_HOME%\bin\javac.exe" -encoding UTF-8 -cp "lib\*" -d "build\classes" @"build\sources.txt"
if errorlevel 1 exit /b 1

echo Creating build\client.jar...
if exist "src\assets" xcopy /e /i /y "src\assets" "build\classes\assets" >nul
set "LIBCP="
for %%L in ("lib\*.jar") do set "LIBCP=!LIBCP! ../lib/%%~nxL"
>"build\manifest.txt" echo Main-Class: unpackaged.Client
if defined LIBCP >>"build\manifest.txt" echo Class-Path:!LIBCP!
"%JAVA_HOME%\bin\jar.exe" cfm "build\client.jar" "build\manifest.txt" -C "build\classes" .
if errorlevel 1 exit /b 1

copy /y "Play-Client.bat.template" "Play-Client.bat" >nul
if errorlevel 1 (
    echo ERROR: Could not create Play-Client.bat. Check folder permissions and retry.
    exit /b 1
)

echo Build complete: build\client.jar
copy /y "Play-Client-From-Root.bat.template" "..\Play-Client.bat" >nul
if errorlevel 1 exit /b 1
echo Ready to play: Play-Client.bat has also been created in the main SoloScape folder.
exit /b 0
