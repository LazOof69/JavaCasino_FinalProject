@echo off
chcp 65001 >nul
cd /d "%~dp0"

if exist JavaCasino.jar (
    java -jar JavaCasino.jar
) else if exist out\casino\Main.class (
    echo Running from compiled classes...
    java -cp out casino.Main
) else (
    echo No build found! Running build first...
    call build.bat
    if exist JavaCasino.jar (
        java -jar JavaCasino.jar
    ) else (
        java -cp out casino.Main
    )
)
