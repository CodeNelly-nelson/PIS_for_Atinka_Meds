@echo off
REM Run Atinka Meds on Windows

REM Go to this script's directory
cd /d "%~dp0"

REM Ensure data dir exists next to the script (not inside jar)
if not exist "data" mkdir "data"

echo Starting Atinka Meds...
echo.

REM Require Java 17+
where java >nul 2>nul
if errorlevel 1 (
  echo ERROR: Java runtime not found. Please install Java 17+ (or add Java to PATH).
  echo.
  pause
  exit /b 1
)

REM Run the jar from .\dist
java -jar "dist\AtinkaMeds.jar"

echo.
echo Program finished. Press any key to close...
pause >nul
