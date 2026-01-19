@echo off
echo ========================================
echo   PORNIRE SISTEM BANCAR COMPLET
echo ========================================
echo.
echo Acest script porneste ambele servere:
echo   - Serverul RMI (backend)
echo   - Serverul Web (frontend)
echo.

REM Verificam daca exista clasele compilate
if not exist "bin\server\BankServer.class" (
    echo EROARE: Clasele nu sunt compilate!
    echo Rulati mai intai: compile.bat
    pause
    exit /b 1
)

if not exist "bin\web\WebServer.class" (
    echo EROARE: Clasele nu sunt compilate!
    echo Rulati mai intai: compile.bat
    pause
    exit /b 1
)

echo [1/2] Pornire server RMI...
start "Server RMI - Sistem Bancar" cmd /k "cd /d %~dp0 && java -cp bin server.BankServer"

echo Asteptam 3 secunde pentru initializarea RMI...
timeout /t 3 /nobreak > nul

echo [2/2] Pornire server Web...
start "Server Web - Sistem Bancar" cmd /k "cd /d %~dp0 && java -cp bin web.WebServer"

echo.
echo ========================================
echo   SERVERE PORNITE!
echo ========================================
echo.
echo Deschideti browserul la: http://localhost:8080
echo.
echo Pentru a opri serverele, inchideti ferestrele de terminal
echo sau apasati Ctrl+C in fiecare fereastra.
echo.

timeout /t 3 /nobreak > nul
start http://localhost:8080
