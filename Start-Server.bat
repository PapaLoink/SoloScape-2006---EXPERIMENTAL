@echo off
call "%~dp0Server\Start-Server.bat" %*
exit /b %ERRORLEVEL%
