@echo off
REM Script para LIMPIAR completamente y RECOMPILAR
REM Solución para error: "Cannot read the array length because arr$" is null

setlocal enabledelayedexpansion

cd /d "%~dp0"

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║   LIMPIEZA PROFUNDA Y RECOMPILACION (HIBERNATEERROR FIXED)    ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Paso 1: Eliminar target
echo [1/4] Eliminando carpeta target...
if exist target (
    rmdir /s /q target
    if exist target (
        echo ❌ ERROR: No se pudo eliminar target
        echo Cierra tu IDE (VSCode, IntelliJ, etc) e intenta nuevamente
        pause
        exit /b 1
    )
    echo ✓ Target eliminado
) else (
    echo ✓ Target no existía
)

REM Paso 2: Maven clean
echo.
echo [2/4] Ejecutando Maven clean...
call mvnw.cmd clean
if %ERRORLEVEL% neq 0 (
    echo ❌ Maven clean falló
    pause
    exit /b 1
)
echo ✓ Maven clean completado

REM Paso 3: Maven install
echo.
echo [3/4] Compilando proyecto (esto puede tardar 2-3 minutos)...
call mvnw.cmd install -DskipTests -q
if %ERRORLEVEL% neq 0 (
    echo ❌ Compilación falló. Ver errores arriba.
    pause
    exit /b 1
)
echo ✓ Compilación exitosa

REM Paso 4: Iniciar
echo.
echo [4/4] Iniciando aplicación...
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║   Abriendo aplicación en puerto 8080                          ║
echo ║   Accede a: http://localhost:8080                             ║
echo ║   Presiona Ctrl+C para detener                                ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

call mvnw.cmd spring-boot:run

echo.
echo Aplicación detenida.
pause
