
echo %~dp0
cd /d %~dp0

call setenv_%computername%.bat
SET BASE_LOCATION=%CD%
echo %JAVA_HOME%
echo %JMETER_HOME%
echo %BASE_LOCATION%

SET "SCENARIO_SCRIPTS_DIR=.\"
SET "SCRIPT_NAME=Mail.jmx"
SET "WORKSPACE_HOME=..\..\"
SET PATH=%JAVA_HOME%\bin;%JMETER_HOME%\bin;%PATH%

CALL :normalise "%SCENARIO_SCRIPTS_DIR%" SCENARIO_SCRIPTS_DIR
CALL :normalise "%WORKSPACE_HOME%" WORKSPACE_HOME
CALL :normalise "%SCRIPT_NAME%" SCRIPT_NAME
SET EG_LT_CLASSPATH=%JMETER_HOME%\lib\*;%JMETER_HOME%\lib\ext\*;%BASE_LOCATION%\JavaCode\lib\*
echo %EG_LT_CLASSPATH%

SET _my_datetime=%date%_%time%
SET _my_datetime=%_my_datetime: =_%
SET _my_datetime=%_my_datetime::=%
SET _my_datetime=%_my_datetime:/=_%
SET _my_datetime=%_my_datetime:.=_%

java -cp %BASE_LOCATION%/JavaCode/classes companyname.lt.common.util.CreateRunCSV "%BASE_LOCATION%\csv_user_input\users.csv" %STARTING_USER_ID% %BASE_LOCATION% %NO_OF_AGENTS% 2 >%BASE_LOCATION%\Logs\CreateRunCSV.log
if NOT ERRORLEVEL 0 GOTO :END

echo %errorlevel%
if NOT ERRORLEVEL 0 GOTO :END

FOR /L %%A IN (1,1,%LOOP%) DO (
echo "Starting Jmeter for Mail testing"
jmeter.bat -JCookieManager.save.cookies=true -Jincludecontroller.prefix=%SCENARIO_SCRIPTS_DIR% -Jlt_server_name=%TARGET_SERVER_NAME% -Jv_request_protocol=%PROTOCOL% -Jlt_conc_user_cnt=%NO_OF_AGENTS% -Jlt_iteration_cnt=1 -Jlt_No_Iteration_perUser_cnt=%ITERATIONS% -Jlt_ramp_up=%AGENT_RAMPUP_DURATION% -Jlt_from_email_address=%lt_from_email_address% -Jlt_to_email_address=%lt_to_email_address% -t %SCRIPT_NAME% -l %SCENARIO_SCRIPTS_DIR%Result\Mail_%computername%_Users_%NO_OF_AGENTS%_Iteration_%%A_at_%_my_datetime%.jtl -e -o %SCENARIO_SCRIPTS_DIR%Result\Mail_%computername%_Users_%NO_OF_AGENTS%_Iteration_%%A_at_%_my_datetime% -j %BASE_LOCATION%\Logs\JMETERLOG%computername%_Users_%NO_OF_AGENTS%_Iteration_%%A_at_%_my_datetime%.log 1>%BASE_LOCATION%\Logs\consoleOutputMail_%computername%_Users_%NO_OF_AGENTS%_Iteration_%%A_at_%_my_datetime%.log 2>%BASE_LOCATION%\Logs\Mail_%computername%_Users_%NO_OF_AGENTS%_Iteration_%%A_at_%_my_datetime%.log

)
GOTO END

:normalise
SET "%2=%~f1"
GOTO :EOF

:END
echo "terminating the batch- due to error in Mail testing"
echo %date% %time%
pause
