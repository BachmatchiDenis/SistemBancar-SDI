@echo off
echo ========================================
echo   PORNIRE SERVER WEB
echo ========================================
echo.

REM Verificam daca exista clasele compilate
if not exist "bin\web\WebServer.class" (
    echo EROARE: Clasele nu sunt compilate!
    echo Rulati mai intai: compile.bat
    pause
    exit /b 1
)

REM Verificam daca serverul RMI este pornit
echo Verificare conexiune RMI...
echo.

set RMI_HOST=localhost
set RMI_PORT=1099
set WEB_PORT=8080

REM Parsam argumentele
:parse_args
if "%~1"=="" goto start_server
if "%~1"=="-webport" (
    set WEB_PORT=%~2
    shift
    shift
    goto parse_args
)
if "%~1"=="-rmihost" (
    set RMI_HOST=%~2
    shift
    shift
    goto parse_args
)
if "%~1"=="-rmiport" (
    set RMI_PORT=%~2
    shift
    shift
    goto parse_args
)
shift
goto parse_args

:start_server
echo Pornire server web pe portul %WEB_PORT%...
echo Conectare la RMI: %RMI_HOST%:%RMI_PORT%
echo.

java -cp bin web.WebServer -webport %WEB_PORT% -rmihost %RMI_HOST% -rmiport %RMI_PORT%
