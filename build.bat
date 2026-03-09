@echo off
chcp 65001 >nul
echo ========================================
echo    Java Casino - Build Script
echo ========================================
echo.

cd /d "%~dp0"

REM Try to find JAVA_HOME
if defined JAVA_HOME (
    set "JAR_CMD=%JAVA_HOME%\bin\jar"
) else (
    REM Try to find jar from javac location
    for %%i in (javac.exe) do set "JAVAC_PATH=%%~$PATH:i"
    if defined JAVAC_PATH (
        for %%i in ("%JAVAC_PATH%") do set "JAR_CMD=%%~dpi..\bin\jar"
    ) else (
        set "JAR_CMD=jar"
    )
)

echo [1/3] Cleaning old build...
if exist out rmdir /s /q out
if exist "JavaCasino.jar" del "JavaCasino.jar"
mkdir out

echo [2/3] Compiling Java files...
javac -d out -encoding UTF-8 ^
    src/casino/util/*.java ^
    src/casino/model/*.java ^
    src/casino/game/card/*.java ^
    src/casino/service/*.java ^
    src/casino/gui/*.java ^
    src/casino/*.java

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo [3/3] Creating JAR file...

REM Create manifest file
echo Main-Class: casino.Main> manifest.txt

REM Create JAR using jar command or manual method
cd out
"%JAR_CMD%" cvfm ../JavaCasino.jar ../manifest.txt casino >nul 2>&1

if %errorlevel% neq 0 (
    echo jar command not found, creating runnable structure...
    cd ..

    REM Create a batch launcher instead
    echo @echo off > JavaCasino_Run.bat
    echo cd /d "%%~dp0" >> JavaCasino_Run.bat
    echo java -cp out casino.Main >> JavaCasino_Run.bat

    echo.
    echo ========================================
    echo    Build Complete!
    echo    JAR creation skipped (jar not in PATH)
    echo    Run: JavaCasino_Run.bat
    echo ========================================
) else (
    cd ..
    del manifest.txt 2>nul
    echo.
    echo ========================================
    echo    Build Complete!
    echo    Run: java -jar JavaCasino.jar
    echo    Or double-click JavaCasino.jar
    echo ========================================
)

pause
