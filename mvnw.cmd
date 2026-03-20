@echo off
setlocal

set "PROJECT_DIR=%~dp0"
set "DEFAULT_JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot"
set "MAVEN_HOME=%PROJECT_DIR%.tools\apache-maven-3.9.14"

if not defined JAVA_HOME (
    if exist "%DEFAULT_JAVA_HOME%\bin\java.exe" (
        set "JAVA_HOME=%DEFAULT_JAVA_HOME%"
    )
)

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo JAVA_HOME is not configured with a valid JDK.
    echo Expected JDK: "%DEFAULT_JAVA_HOME%"
    exit /b 1
)

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo Local Maven was not found in "%MAVEN_HOME%".
    echo Reopen the workspace or download the local Maven files again.
    exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"
call "%MAVEN_HOME%\bin\mvn.cmd" %*
