::
:: Copyright (c) 1998, 2024, Oracle and/or its affiliates. All rights reserved.
::
@echo off

setlocal

for %%I in (.) do set SAMPLE_NAME=%%~nxI
set ORDER="MyShareable ClientApplet ServerApplet AggregatorApplet"

@echo Sample name: [%SAMPLE_NAME%]
@echo.

pushd..

call build.bat %SAMPLE_NAME% %ORDER%
