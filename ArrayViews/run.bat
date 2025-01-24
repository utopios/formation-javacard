::
:: Copyright (c) 1998, 2024, Oracle and/or its affiliates. All rights reserved.
::
@echo off

setlocal

:: Evaluate samples dir out of current directory (parent dir)
for %%I in (.) do set SAMPLES_DIR=%%~pdI
:: Evaluate sample name out of current directory, too
for %%I in (.) do set SAMPLE_NAME=%%~nxI

@echo Sample name: [%SAMPLE_NAME%]
@echo.

set CLIENT_NAME=com.oracle.javacard.sample.ClientArrayViews

set CAP_CLI=%SAMPLES_DIR%\%SAMPLE_NAME%\applet\deliverables\ClientApplet\com\oracle\jcclassic\samples\arrayview\ClientApplet\javacard\ClientApplet.cap
set CAP_SRV=%SAMPLES_DIR%\%SAMPLE_NAME%\applet\deliverables\ServerApplet\com\oracle\jcclassic\samples\arrayview\ServerApplet\javacard\ServerApplet.cap
set CAP_SHR=%SAMPLES_DIR%\%SAMPLE_NAME%\applet\deliverables\MyShareable\com\oracle\jcclassic\samples\arrayview\MyShareable\javacard\MyShareable.cap
set PROPS=%JC_HOME_SIMULATOR%\samples\client.config.properties

set ARGS_CLIENT="-capClient=%CAP_CLI% -capServer=%CAP_SRV% -capShared=%CAP_SHR% -props=%PROPS%"

pushd..
call run.bat %SAMPLE_NAME% %CLIENT_NAME% %ARGS_CLIENT%

