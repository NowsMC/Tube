@echo off
setlocal
cd /d "%~dp0"

set "JAVA=java"
if defined JAVA_HOME set "JAVA=%JAVA_HOME%\bin\java"

"%JAVA%" -Xms1G -Xmx2G -jar "@{tube.server.jar}" %*
pause
