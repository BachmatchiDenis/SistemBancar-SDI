@echo off
echo ========================================
echo   PORNIRE SERVER BANCAR RMI
echo ========================================
echo.

REM Verificam daca exista clasele compilate
if not exist "bin\server\BankServer.class" (
    echo EROARE: Clasele nu sunt compilate!
    echo Rulati mai intai: compile.bat
    pause
    exit /b 1
)

echo Pornire server pe portul implicit 1099...
echo Pentru a folosi alt port, rulati: run_server.bat [port]
echo.
echo Apasati Ctrl+C pentru a opri serverul.
echo.

REM Pornim serverul
java -cp bin server.BankServer %1
