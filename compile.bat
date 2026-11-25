@echo off
REM ============================================================
REM  SIBU — Script de compilacion y lanzamiento
REM  Java 8  |  MariaDB 13.0  |  Sin Maven
REM ============================================================

SET JAVA_EXE=C:\Program Files\Java\jre-1.8\bin\java.exe
SET JAVAC_EXE=javac
SET MARIA_DIR=C:\Program Files\MariaDB 13.0\bin
SET MYSQL_EXE=%MARIA_DIR%\mysql.exe
SET DB_USER=root
SET DB_PASS=1234
SET DB_NAME=sibu_db

echo.
echo [1/4] Verificando entorno...
where javac >nul 2>&1 || (echo ERROR: javac no encontrado en PATH. && pause && exit /b 1)
IF NOT EXIST "%JAVA_EXE%" (echo ERROR: JRE 1.8 no encontrado en %JAVA_EXE%. && pause && exit /b 1)
echo     javac OK
echo     java  OK

echo.
echo [2/4] Compilando fuentes Java 8...
IF NOT EXIST target\classes mkdir target\classes

REM Recolectar todos los .java
dir /s /b src\main\java\*.java > _sources.txt
%JAVAC_EXE% -source 1.8 -target 1.8 -encoding UTF-8 -cp "lib\*" -d target\classes @_sources.txt
IF ERRORLEVEL 1 (
    del /q _sources.txt
    echo ERROR: Compilacion fallida. Revisa los errores anteriores.
    pause
    exit /b 1
)
del /q _sources.txt
echo     Compilacion exitosa.

REM Copiar recursos
IF EXIST src\main\resources (
    xcopy /E /Y /Q src\main\resources\* target\classes\ >nul 2>&1
)

echo.
echo [3/4] Compilando pruebas y ejecutando...
IF NOT EXIST target\test-classes mkdir target\test-classes
dir /s /b src\test\java\*.java > _tsources.txt
%JAVAC_EXE% -source 1.8 -target 1.8 -encoding UTF-8 -cp "target\classes;lib\*" -d target\test-classes @_tsources.txt
del /q _tsources.txt
IF ERRORLEVEL 1 (
    echo ADVERTENCIA: Compilacion de pruebas fallida. Se omiten.
    goto :lanzar
)
"%JAVA_EXE%" -cp "target\classes;target\test-classes;lib\*" com.sibu.TestRunner
IF ERRORLEVEL 1 (
    echo ADVERTENCIA: Algunas pruebas fallaron. Revisa el log.
)

:lanzar
echo.
echo [4/4] Iniciando SIBU...
echo     Recuerda que MariaDB debe estar en ejecucion.
echo     Para iniciar MariaDB (como Administrador):
echo       "%MARIA_DIR%\mysqld.exe" --console
echo     Para cargar el schema:
echo       "%MYSQL_EXE%" -u %DB_USER% -p%DB_PASS% < database.sql
echo.
"%JAVA_EXE%" -cp "target\classes;lib\*" com.sibu.App
pause
