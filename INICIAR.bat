@echo off
REM Script para iniciar la aplicacion Rol

cd /d "%~dp0"

echo.
echo ===============================================
echo Iniciando aplicacion Rol en Spring Boot
echo ===============================================
echo.
echo Verificando que la compilacion existe...
echo.

if not exist "target\rol-0.0.1-SNAPSHOT.jar" (
    echo ERROR: El JAR no existe. Primero debes ejecutar COMPILAR.bat
    echo.
    pause
    exit /b 1
)

echo Iniciando en puerto 8080...
echo Base de datos: MySQL en localhost:3306
echo.

call mvnw.cmd spring-boot:run

echo.
echo Aplicacion detenida.
echo.
pause
