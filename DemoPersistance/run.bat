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

set CLIENT_NAME=com.utopios.client.ScaffoldingClient

set CAP=%SAMPLES_DIR%\%SAMPLE_NAME%\applet\deliverables\Scaffolding\com\utopios\scaffolding\javacard\Scaffolding.cap
set PROPS=%JC_HOME_SIMULATOR%\samples\client.config.properties

set ARGS_CLIENT="-cap=%CAP% -props=%PROPS%"

pushd..
call run.bat %SAMPLE_NAME% %CLIENT_NAME% %ARGS_CLIENT%

