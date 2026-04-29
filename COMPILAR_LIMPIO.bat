@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

echo.
echo ===============================================
echo COMPILACION LIMPIA - D&D Role Playing Game
echo ===============================================
echo.

call mvnw.cmd clean compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ===============================================
    echo ✅ COMPILACION EXITOSA!
    echo ===============================================
    echo.
    echo Proximos pasos:
    echo 1. Ejecuta: mvnw.cmd spring-boot:run
    echo 2. Accede a: http://localhost:8080
    echo.
) else (
    echo.
    echo ===============================================
    echo ❌ COMPILACION FALLIDA
    echo ===============================================
    echo.
)

pause
