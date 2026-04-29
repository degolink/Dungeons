@echo off
REM Script para compilar y ejecutar la aplicación Rol
REM Corrección: Hibernate now has @NoArgsConstructor on all entities

cd /d "%~dp0"

echo.
echo ===============================================
echo Limpiando compilacion anterior...
echo ===============================================
call mvnw.cmd clean

echo.
echo ===============================================
echo Compilando proyecto Rol (Sin Tests)...
echo ===============================================
call mvnw.cmd install -DskipTests

if %ERRORLEVEL% neq 0 (
    echo.
    echo ERROR durante compilacion!
    pause
    exit /b 1
)

echo.
echo ===============================================
echo COMPILACION EXITOSA!
echo ===============================================
echo.
echo Iniciando aplicacion en puerto 8080...
echo.

call mvnw.cmd spring-boot:run

echo.
echo Aplicacion detenida.
pause
