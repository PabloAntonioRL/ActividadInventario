@echo off
title Actualizacion Git
setlocal

echo ==========================================
echo          ACTUALIZACION DEL PROYECTO
echo ==========================================
echo.


REM ==========================================
REM Verificar repositorio Git
REM ==========================================

git rev-parse --is-inside-work-tree >nul 2>&1

if errorlevel 1 (
    echo ERROR: Esta carpeta no es un repositorio Git.
    echo.
    pause
    exit /b 1
)

REM ==========================================
REM Mostrar estado actual
REM ==========================================

echo Archivos con cambios:
echo.
git status




REM ==========================================
REM Verificar si existen cambios
REM ==========================================

git status --porcelain | findstr /r /c:"." >nul

if errorlevel 1 (
    echo No hay cambios para actualizar.
    echo.
    pause
    exit /b 0
)

REM ==========================================
REM Confirmar actualizacion
REM ==========================================

set /p confirm=^¿Deseas continuar con estos cambios? (S/N): 

if /I not "%confirm%"=="S" (
    echo.
    echo Operacion cancelada.
    pause
    exit /b 0
)

echo.

REM ==========================================
REM Agregar todos los cambios
REM ==========================================

echo Agregando archivos...
git add -A

if errorlevel 1 (
    echo.
    echo ERROR: No se pudieron agregar los archivos.
    pause
    exit /b 1
)

REM ==========================================
REM Mostrar lo que realmente se va a commitear
REM ==========================================

echo.
echo ==========================================
echo       ARCHIVOS A INCLUIR EN EL COMMIT
echo ==========================================
echo.

git status --short

echo.
echo ==========================================
echo.

REM ==========================================
REM Solicitar mensaje
REM ==========================================

set /p commitMessage=Escribe el mensaje del commit: 

if "%commitMessage%"=="" (
    echo.
    echo ERROR: Debes ingresar un mensaje para el commit.
    pause
    exit /b 1
)

REM ==========================================
REM Crear commit
REM ==========================================

echo.
echo Creando commit...
echo.

git commit -m "%commitMessage%"

if errorlevel 1 (
    echo.
    echo ERROR: No se pudo crear el commit.
    pause
    exit /b 1
)

REM ==========================================
REM Push
REM ==========================================

echo.
echo ==========================================
echo             EJECUTANDO PUSH
echo ==========================================
echo.

git push --set-upstream origin master

if errorlevel 1 (
    echo.
    echo ERROR: No se pudo realizar el push.
    pause
    exit /b 1
)

echo.
echo ==========================================
echo       ACTUALIZACION COMPLETADA
echo ==========================================
echo.

pause