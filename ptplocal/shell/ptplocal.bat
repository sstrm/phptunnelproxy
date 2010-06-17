@echo off
if "%PTP_HOME%"=="" set PTP_HOME=%~dp0

if '%1=='## goto ENVSET

set PTP_JAR="%PTP_HOME%lib"
for %%c in ("%PTP_HOME%lib\*.jar") do call %0 ## "%%c"

GOTO RUN

:RUN
cd %PTP_HOME%
if exist "%PTP_HOME%\jre" set JAVA_HOME=%PTP_HOME%\jre
if "%JAVA_HOME%" == "" goto NOJAVAHOME
if exist "%JAVA_HOME%\..\jre6" set JAVA_HOME=%JAVA_HOME%\..\jre6
start "PTP Local" "%JAVA_HOME%\bin\javaw.exe" -cp %PTP_JAR% ptp.ui.SwingLauncher %*
goto END

:NOJAVAHOME
start "PTP Local" javaw.exe -cp %PTP_JAR% ptp.ui.SwingLauncher %*
goto END

:ENVSET
set PTP_JAR=%PTP_JAR%;%2
goto END

:END
