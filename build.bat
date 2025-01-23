::
:: Copyright (c) 1998, 2024, Oracle and/or its affiliates. All rights reserved.
::

@echo off

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

:: Check environment if JC_HOME_TOOLS is set properly
if not defined JC_HOME_TOOLS (
    @echo environment [JC_HOME_TOOLS] is not set. Please set it to point to latest Java Card Development Kit Tools
    exit /b 1
)
:: Remove quotation marks if any
set JC_HOME_TOOLS=%JC_HOME_TOOLS:"=%

if not exist "%JC_HOME_TOOLS%"\lib\tools.jar (
   @echo Invalid environment [JC_HOME_TOOLS]. Please set it to point to latest Java Card Development Kit Tools
   exit /b 1
)

setlocal enabledelayedexpansion

:: Generated file list of Java files to be compiled
set JAVA_FILES=java_files.lst

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

::=================
:: Checks complete
::=================
pushd %SAMPLE_NAME%\applet\src

:: Collect applet's Java files
dir /s /b *.java > %JAVA_FILES%
@echo Java file(s) to be compiled:
for /F %%F in (%JAVA_FILES%) do echo.  (*) %%F

::===============================
:: Java compilation of applet(s)
::===============================
@echo.
@echo Building Java Card applet(s) for: [%SAMPLE_NAME%] ...

set COMMAND_JAVAC="%JAVA_HOME%"\bin\javac -g -d ..\bin -source 7 -target 7 ^
-cp "%JC_HOME_TOOLS%\lib\api_classic-3.2.0.jar;%JC_HOME_TOOLS%\lib\api_classic_annotations-3.2.0.jar" ^
-Xlint:-options @%JAVA_FILES%
@echo.
@echo Executing: [%COMMAND_JAVAC%]
@echo.
%COMMAND_JAVAC%

:: Store result value before removing list of Java files for check later on
set RESULT=%ERRORLEVEL%
del %JAVA_FILES%
popd

if %RESULT% neq 0 (
    @echo Stopping due to error^(s^) in Java compilation
    exit /b %RESULT%
)

::========================================================
:: Check for ordered package list to be used by converter
::========================================================
pushd %SAMPLE_NAME%\applet
set PACKAGE_LIST=%CD%\PackageList.lst
if exist %PACKAGE_LIST% del %PACKAGE_LIST%

:: No ordered list given
if [%2] == [] goto :no_package_order

:: Evaluate packages (i. e. remove quotes)
for %%I in (%2) do set PACKAGES=%%~I

:loop_packages
:: Evaluate given package list
for /f "tokens=1*" %%a in ("%PACKAGES%") do (
   :: Attention: Make sure that no trailing blanks are written to file!
   echo %%a>>%PACKAGE_LIST%
   set PACKAGES=%%b
)
if defined PACKAGES goto :loop_packages

:no_package_order
:: No ordered package list created above - create list from existing configuration files instead
if not exist %PACKAGE_LIST% (
    for %%I in (.\configurations\*.conf) do (
        :: Attention: Make sure that no trailing blanks are written to file!
        echo %%~nI>>%PACKAGE_LIST%
    )
)

popd

::=================
:: Converter stage
::=================
@echo Running Java Card converter and verifier ...
@echo.

@echo Java Card package(s) to be converted and verified:
for /F %%F in (%PACKAGE_LIST%) do echo.  (*) %%F
@echo.

pushd %SAMPLE_NAME%\applet\bin

for /F "tokens=* delims=" %%I in (%PACKAGE_LIST%) do (
    call "%JC_HOME_TOOLS%"\bin\converter.bat -config ..\configurations\%%I.conf
    if !ERRORLEVEL! neq 0 goto break_converter_verifier_loop

    :: We explicitly have to search exp files because samples are slightly different
    for /R ..\deliverables %%G in (%%I*.exp) do (
        call "%JC_HOME_TOOLS%"\bin\verifyexp.bat %%G
        if !ERRORLEVEL! neq 0 goto break_converter_verifier_loop
    )
)

:break_converter_verifier_loop
set RESULT=%ERRORLEVEL%

:: Remove automatically created package list file if any, also in error case
if exist %PACKAGE_LIST% del %PACKAGE_LIST%
popd

if %RESULT% neq 0 exit /b %RESULT%

::============================
:: Java compilation of client
::============================
@echo.
@echo Building Java client for: [%SAMPLE_NAME%] ...

pushd %SAMPLE_NAME%\client\src

:: Collect client's Java files
dir /s /b *.java > %JAVA_FILES%
@echo Java file(s) to be compiled:
for /F %%F in (%JAVA_FILES%) do echo.  (*) %%F

set COMMAND_JAVAC="%JAVA_HOME%"\bin\javac -g -d ..\bin ^
-cp "%JC_HOME_SIMULATOR%\client\AMService\amservice.jar;%JC_HOME_SIMULATOR%\client\COMService\socketprovider.jar" ^
-Xlint:-options @%JAVA_FILES%
@echo.
@echo Executing: [%COMMAND_JAVAC%]
@echo.
%COMMAND_JAVAC%

:: Store result value before doing anything else
set RESULT=%ERRORLEVEL%
del %JAVA_FILES%

if %RESULT% equ 0 (
    @echo Success
) else (
    @echo Build of [%1] failed
)

exit /b %RESULT%
