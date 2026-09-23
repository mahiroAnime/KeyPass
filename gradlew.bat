@echo off
setlocal
set "DIR=%~dp0"
set "GRADLE_VERSION=8.14.3"
set "CACHE=%DIR%.gradle-wrapper"
set "DIST=%CACHE%\gradle-%GRADLE_VERSION%"
set "ZIP=%CACHE%\gradle-%GRADLE_VERSION%-bin.zip"

if not exist "%CACHE%" mkdir "%CACHE%"
if not exist "%DIST%\bin\gradle.bat" (
  if not exist "%ZIP%" (
    echo Downloading Gradle %GRADLE_VERSION%...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -UseBasicParsing 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%ZIP%'"
    if errorlevel 1 (
      echo Failed to download Gradle.
      exit /b 1
    )
  )
  echo Extracting Gradle...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force '%ZIP%' '%CACHE%'"
  if errorlevel 1 exit /b 1
)
call "%DIST%\bin\gradle.bat" %*
exit /b %ERRORLEVEL%
