@echo off
setlocal
title KeyPass - Fabric 1.21.7 Build
echo ==========================================
echo   KeyPass - Fabric 1.21.7
echo ==========================================
echo.
where java >nul 2>nul
if errorlevel 1 (
  echo ERROR: Java was not found.
  echo Install Java 21 JDK and make sure "java" is in PATH.
  pause
  exit /b 1
)
java -version
echo.
echo Building mod...
call "%~dp0gradlew.bat" build
if errorlevel 1 (
  echo.
  echo BUILD FAILED.
  pause
  exit /b 1
)
echo.
echo BUILD SUCCESSFUL!
echo.
echo JAR files:
dir /b "%~dp0build\libs\*.jar"
echo.
echo Copy the main .jar (not -sources.jar) to your Minecraft mods folder.
pause
