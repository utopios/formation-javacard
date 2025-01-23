::
:: Copyright (c) 1998, 2024, Oracle and/or its affiliates. All rights reserved.
::

@echo off

::=======================================================================
:: Expected arguments:
::   %1 : Sample name (e. g. ArrayViews)
::   %2 : Client name (e. g. ClientArrayViews)
::   %3 : Client args (cap file(s) and configuration file)
::=======================================================================


if "%1" equ "" (
    @echo No parameters given
    @echo Usage: build.bat "sample name" ^(e. g. "ArrayViews"^)
    exit /b 1
)

:: Check environment if JAVA_HOME is set properly
if not defined JAVA_HOME (
    @echo environment [JAVA_HOME] is not set. Please set it to point to JDK 17
    exit /b 1
)
:: Remove quotation marks if any
set JAVA_HOME=%JAVA_HOME:"=%

:: Check environment if JC_HOME_SIMULATOR is set properly
if not defined JC_HOME_SIMULATOR (
    @echo environment [JC_HOME_SIMULATOR] is not set. Please set it to point to latest Java Card Development Kit Simulator
    exit /b 1
)

:: Remove quotation marks if any
set JC_HOME_SIMULATOR=%JC_HOME_SIMULATOR:"=%

if not exist "%JC_HOME_SIMULATOR%"\runtime\bin (
    @echo Invalid environment [JC_HOME_SIMULATOR]. Please set it to point to latest Java Card Development Kit Simulator
    exit /b 1
)

setlocal

::=======================================================================
:: Look up existing sample and verify that given name (%1) really exists
::=======================================================================
for /D %%D in (*) do (
    if "%1" equ "%%~nxD" (
        set SAMPLE_NAME=%1
        goto sample_name_match
    )
)
@echo Unknown sample name given [%1]
exit /b 1

:sample_name_match
set SAMPLE_NAME=%1

:: Evaluate arguments (i. e. remove quotes)
for %%I in (%3) do set ARGS_CLIENT=%%~I

::=================
:: Checks complete
::=================

@echo Sample name  : [%SAMPLE_NAME%]
@echo Client name  : [%2]
@echo Argument list:

set args=%ARGS_CLIENT%
:loop_args
for /f "tokens=1*" %%a in ("%args%") do (
   @echo. %%a
   set args=%%b
)
if defined args goto :loop_args

pushd %SAMPLE_NAME%\client\bin
set EXT_MODULEPATH="%JC_HOME_SIMULATOR%\client\AMService;%JC_HOME_SIMULATOR%\client\COMService"
set COMMAND_JAVA="%JAVA_HOME%"\bin\java ^
-p "%JC_HOME_SIMULATOR%\client\AMService\amservice.jar;%JC_HOME_SIMULATOR%\client\COMService\socketprovider.jar" ^
--module-path %EXT_MODULEPATH% --add-modules ALL-MODULE-PATH %2 %ARGS_CLIENT%

@echo.
@echo Executing: [%COMMAND_JAVA%]
@echo.
%COMMAND_JAVA%

@echo.
if %ERRORLEVEL% EQU 0 (
    @echo Success
) else (
    @echo Execution of [%2] failed
)

exit /b %ERRORLEVEL%
