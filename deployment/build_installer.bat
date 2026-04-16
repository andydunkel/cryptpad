@echo off
setlocal

set SIGNTOOL="C:\Program Files (x86)\Windows Kits\10\bin\10.0.22621.0\x64\signtool.exe"
set SCRIPT=cryptpad-installer.nsi
set OUTPUT=installer\cryptpad-setup.exe

echo [1/3] Building installer...
makensis "%SCRIPT%"
if %ERRORLEVEL% neq 0 (
    echo ERROR: makensis failed.
    exit /b %ERRORLEVEL%
)

set /p SIGN="[2/3] Sign installer? (Y/N): "
if /i "%SIGN%" neq "Y" (
    echo Signing skipped.
    goto :done
)

echo Signing installer...
%SIGNTOOL% sign ^
  /fd SHA256 ^
  /tr http://timestamp.digicert.com ^
  /td SHA256 ^
  /n "Andy Dunkel und Daniel Iwer" ^
  "%OUTPUT%"
if %ERRORLEVEL% neq 0 (
    echo ERROR: Signing failed.
    exit /b %ERRORLEVEL%
)

echo [3/3] Verifying signature...
%SIGNTOOL% verify /pa /v "%OUTPUT%"
if %ERRORLEVEL% neq 0 (
    echo ERROR: Signature verification failed.
    exit /b %ERRORLEVEL%
)

:done
echo.
echo Done: %OUTPUT%
endlocal