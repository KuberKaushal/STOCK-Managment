@echo off
echo ===================================================
echo Compiling Distributed Inventory & Billing System...
echo ===================================================
if not exist bin mkdir bin
javac -d bin -cp "lib/junit-platform-console-standalone.jar;src" src/com/vityarthi/inventory/model/*.java src/com/vityarthi/inventory/exception/*.java src/com/vityarthi/inventory/util/*.java src/com/vityarthi/inventory/service/*.java src/com/vityarthi/inventory/*.java src/com/vityarthi/inventory/test/*.java
if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)
echo Compilation successful! Launching console application...
echo.
java -cp bin com.vityarthi.inventory.Main
pause