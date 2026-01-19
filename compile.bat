@echo off
echo ========================================
echo   COMPILARE SISTEM BANCAR WEB
echo ========================================
echo.

REM Cream directorul pentru clase compilate
if not exist "bin" mkdir bin

echo [1/4] Compilare clase comune...
javac -d bin common\*.java
if %errorlevel% neq 0 (
    echo EROARE: Compilarea claselor comune a esuat!
    pause
    exit /b 1
)
echo       OK

echo [2/4] Compilare server RMI...
javac -d bin -cp bin server\*.java
if %errorlevel% neq 0 (
    echo EROARE: Compilarea serverului RMI a esuat!
    pause
    exit /b 1
)
echo       OK

echo [3/4] Compilare server Web...
javac -d bin -cp bin web\*.java
if %errorlevel% neq 0 (
    echo EROARE: Compilarea serverului Web a esuat!
    pause
    exit /b 1
)
echo       OK

echo [4/4] Compilare client desktop (optional)...
javac -d bin -cp bin client\*.java 2>nul
if %errorlevel% neq 0 (
    echo       SKIP (optional)
) else (
    echo       OK
)

echo.
echo ========================================
echo   COMPILARE FINALIZATA CU SUCCES!
echo ========================================
echo.
echo Pentru a porni sistemul:
echo   1. run_server.bat  - Porneste serverul RMI (backend)
echo   2. run_web.bat     - Porneste serverul Web (frontend)
echo   3. Deschideti browserul la http://localhost:8080
echo.
echo SAU folositi: start_all.bat pentru a porni totul automat
echo.
pause
