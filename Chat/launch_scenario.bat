@echo OFF
echo %~dp0
cd /d %~dp0

call setenv_%computername%.bat
SET BASE_LOCATION=%CD%
echo %JAVA_HOME%
echo %JMETER_HOME%
echo %BASE_LOCATION%

SET "SCENARIO_SCRIPTS_DIR=.\"
SET "SCRIPT_NAME=Chat_M16.jmx"
SET "WORKSPACE_HOME=..\..\"
SET PATH=%JAVA_HOME%\bin;%JMETER_HOME%\bin;%PATH%

CALL :normalise "%SCENARIO_SCRIPTS_DIR%" SCENARIO_SCRIPTS_DIR
CALL :normalise "%WORKSPACE_HOME%" WORKSPACE_HOME
CALL :normalise "%SCRIPT_NAME%" SCRIPT_NAME
@rem set JVM_ARGS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000

SET EG_LT_CLASSPATH=%JMETER_HOME%\lib\*;%JMETER_HOME%\lib\ext\*;%BASE_LOCATION%\JavaCode\lib\*
echo %EG_LT_CLASSPATH%
del /Q %BASE_LOCATION%\JavaCode\classes\companyname\lt\chat 
rmdir %BASE_LOCATION%\JavaCode\classes\companyname\lt\chat
rmdir %BASE_LOCATION%\JavaCode\classes\companyname\lt
rmdir %BASE_LOCATION%\JavaCode\classes\companyname

SET _my_datetime=%date%_%time%
SET _my_datetime=%_my_datetime: =_%
SET _my_datetime=%_my_datetime::=%
SET _my_datetime=%_my_datetime:/=_%
SET _my_datetime=%_my_datetime:.=_%

javac -cp %EG_LT_CLASSPATH% -d %BASE_LOCATION%\JavaCode\classes -sourcepath %BASE_LOCATION%\JavaCode\src %BASE_LOCATION%\JavaCode\src\companyname\lt\chat\*.java
if NOT ERRORLEVEL 0 GOTO :END
javac -cp %EG_LT_CLASSPATH% -d %BASE_LOCATION%\JavaCode\classes -sourcepath %BASE_LOCATION%\JavaCode\src %BASE_LOCATION%\JavaCode\src\companyname\lt\common\util\*.java
if NOT ERRORLEVEL 0 GOTO :END
java -cp %BASE_LOCATION%/JavaCode/classes companyname.lt.common.util.CreateRunCSV "%BASE_LOCATION%\all_user_csv\Agent Logins.csv" %STARTING_USER_ID% %BASE_LOCATION% %NO_OF_AGENTS_CUSTS% 2 >%BASE_LOCATION%\Logs\CreateRunCSV.log
if NOT ERRORLEVEL 0 GOTO :END
java -cp %BASE_LOCATION%/JavaCode/classes companyname.lt.common.util.CreateRunCustomerCSV "%BASE_LOCATION%\all_user_csv\Customer Logins.csv" %STARTING_USER_ID% %BASE_LOCATION% %NO_OF_AGENTS_CUSTS% 2 %CUSTOMER_MULITIFICATION_FACTOR% >%BASE_LOCATION%\Logs\CreateRunCustomerCSV.log
if NOT ERRORLEVEL 0 GOTO :END

FOR /L %%A IN (1,1,%LOOP%) DO (
echo "Starting Jmeter"
%JMETER_HOME%\bin\jmeter.bat -Juser.classpath=%BASE_LOCATION%\JavaCode\classes;%BASE_LOCATION%\JavaCode\lib -JCookieManager.save.cookies=true -Jp_dept_id=1000 -Jp_context_root=system -Jp_cust_mult_factor=%CUSTOMER_MULITIFICATION_FACTOR% -Jp_DocPath="%BASE_LOCATION%\Agent Attachments\AgentDoc.pdf" -Jp_ImagePath="%BASE_LOCATION%\Agent Attachments\AgentImage.png" -Jp_request_protocol=%PROTOCOL% -Jp_server_name=%TARGET_SERVER_NAME% -Jp_num_agents_cust=%NO_OF_AGENTS_CUSTS% -Jp_agentRampUp=%AGENT_RAMPUP_DURATION% -Jp_custRampUp=%CUSTOMER_RAMPUP_DURATION% -Jp_numOfIterations=%ITERATIONS% -Jp_cust_max_message=%NO_OF_CUSTOMER_MSGS% -Jp_agent_max_message=%NO_OF_AGENT_MSGS% -Jp_agent_not_available=true -Jp_logging_enabled=%LOGGING_ON% -Ljmeter.engine=ERROR -n -t "%SCRIPT_NAME%" -l %BASE_LOCATION%\Logs\Result%_my_datetime%_0288.jtl -e -o %BASE_LOCATION%\Logs\ResultHTML%_my_datetime%_0288 -j %BASE_LOCATION%\Logs\jmeter%_my_datetime%_%%A.log  1>%BASE_LOCATION%\Logs\consoleOutput%_my_datetime%_%%A.log 2>%BASE_LOCATION%\Logs\consoleErr%_my_datetime%_%%A.log
)
GOTO END

:normalise
SET "%2=%~f1"
GOTO :EOF

:END
echo "terminating the batch"
echo %date% %time%
pause
exit
