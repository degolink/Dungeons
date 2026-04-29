@echo off
REM Script para compilar e instalar el proyecto
REM Este script resuelve todos los errores de dependencias

cd /d "%~dp0"

echo.
echo ===============================================
echo Compilando proyecto Rol con Maven...
echo ===============================================
echo.

REM Limpiar y construir omitiendo tests
call mvnw.cmd clean install -DskipTests

if %ERRORLEVEL% == 0 (
    echo.
    echo ===============================================
    echo EXITO! El proyecto se compilo correctamente
    echo ===============================================
    echo.
    echo Proximos pasos:
    echo 1. Asegúrate de que MySQL está corriendo en localhost:3306
    echo 2. Abre una terminal en la carpeta del proyecto
    echo 3. Ejecuta: mvnw.cmd spring-boot:run
    echo 4. Accede a la app en: http://localhost:8080
    echo.
) else (
    echo.
    echo ===============================================
    echo ERROR durante la compilacion
    echo ===============================================
    echo.
    pause
)
