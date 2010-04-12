@echo on
if "%PTP_HOME%"=="" set PTP_HOME=%~dp0

if '%1=='## goto ENVSET

set PTP_JAR="%PTP_HOME%lib"
for %%c in ("%PTP_HOME%lib\*.jar") do call %0 ## "%%c"

GOTO RUN

:RUN
cd %PTP_HOME%
start javaw -cp %PTP_JAR% ptp.ui.GUILauncher %*
goto END

:ENVSET
set PTP_JAR=%PTP_JAR%;%2
goto END

:END
