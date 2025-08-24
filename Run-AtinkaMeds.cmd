@echo off
setlocal

REM === Resolve to this script's folder ===
set "BASE=%~dp0"
pushd "%BASE%" >nul

REM === Prefer bundled JRE, otherwise system java ===
if exist "%BASE%jre\bin\java.exe" (
  set "JAVA=%BASE%jre\bin\java.exe"
) else (
  set "JAVA=java"
)

REM === Jar path ===
set "JAR=%BASE%dist\AtinkaMeds.jar"
if not exist "%JAR%" (
  echo [ERROR] Jar not found: %JAR%
  echo Build the jar into dist\AtinkaMeds.jar (see README).
  echo.
  echo Press any key to close...
  pause >nul
  exit /b 1
)

REM === Ensure data folder exists ===
if not exist "%BASE%data" mkdir "%BASE%data"

echo Starting Atinka Meds...
"%JAVA%" -jar "%JAR%"
set "CODE=%ERRORLEVEL%"

echo.
if not "%CODE%"=="0" echo Program exited with code %CODE%
echo Program finished. Press any key to close...
pause >nul

popd >nul
exit /b %CODE%
