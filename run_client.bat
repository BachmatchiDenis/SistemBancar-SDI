@echo off
echo ========================================
echo   PORNIRE CLIENT BANCAR
echo ========================================
echo.

REM Verificam daca exista clasele compilate
if not exist "bin\client\BankClient.class" (
    echo EROARE: Clasele nu sunt compilate!
    echo Rulati mai intai: compile.bat
    pause
    exit /b 1
)

echo Pornire client...
echo.

REM Pornim clientul
java -cp bin client.BankClient
