@echo off

set APP_NAME=sprint2
set SRC_DIR=src\main\java
set WEB_DIR=src\main\webapp
set TEMP_DIR=TEMP
set LIB_DIR=lib
set APP_DIR=app
set TOMCAT_WEBAPPS=C:\Program Files\Apache Software Foundation\Tomcat 11.0\webapps
set SERVLET_API_JAR=%LIB_DIR%\jakarta.servlet-api-6.0.0.jar
set FRAMEWORK_JAR=%LIB_DIR%\sprint-mvc-framework.jar

if not exist "%FRAMEWORK_JAR%" (
    echo Erreur : %FRAMEWORK_JAR% introuvable.
    echo Construisez d'abord le framework puis copiez son JAR dans %LIB_DIR%.
    exit /b 1
)

REM Suppression et recreation du dossier TEMP
if exist %TEMP_DIR% rmdir /s /q %TEMP_DIR%
mkdir %TEMP_DIR%\WEB-INF\classes
mkdir %TEMP_DIR%\WEB-INF\lib

REM Compilation des fichiers Java avec le JAR du framework et le JAR des Servlets
if exist %SRC_DIR%\*.java (
    rem Genere la liste des fichiers source avec des chemins relatifs
    (for %%f in (%SRC_DIR%\*.java) do @echo %SRC_DIR%\%%~nxf) > sources.txt
    echo Compilation en cours...
    javac -cp "%SERVLET_API_JAR%;%FRAMEWORK_JAR%" -d "%TEMP_DIR%\WEB-INF\classes" @sources.txt 2> compile_errors.txt
    if errorlevel 1 (
        echo Erreur de compilation !
        type compile_errors.txt
        type sources.txt
        exit /b 1
    )
    del sources.txt
    del compile_errors.txt
) else (
    echo Aucun fichier source Java trouve dans %SRC_DIR%.
)

REM Preparer un fichier d'exclusion pour ignorer web.xml
cd %WEB_DIR%
echo web.xml > exclude.txt
cd %~dp0
xcopy %WEB_DIR% %TEMP_DIR% /E /I /Y /EXCLUDE:%WEB_DIR%\exclude.txt
cd %WEB_DIR%
del exclude.txt
cd %~dp0

REM Copier web.xml dans TEMP/WEB-INF
copy /Y "%WEB_DIR%\web.xml" "%TEMP_DIR%\WEB-INF\web.xml"

REM Copier les JAR applicatifs (exclure l'API Servlet qui est fournie par Tomcat)
for %%f in ("%LIB_DIR%\*.jar") do (
    if /I not "%%~nxf"=="jakarta.servlet-api-6.0.0.jar" copy /Y "%%f" "%TEMP_DIR%\WEB-INF\lib\"
)

REM Creation du dossier app pour stocker le .war
if not exist %APP_DIR% mkdir %APP_DIR%

REM Creation du fichier .war dans le dossier app
cd %TEMP_DIR%
jar -cvf ..\%APP_DIR%\%APP_NAME%.war *
cd ..

REM Suppression de l'ancien .war dans le dossier Tomcat s'il existe
if exist "%TOMCAT_WEBAPPS%\%APP_NAME%.war" del /f /q "%TOMCAT_WEBAPPS%\%APP_NAME%.war"

REM Supprimer aussi le dossier d'application explose (si Tomcat l'a decompresse)
if exist "%TOMCAT_WEBAPPS%\%APP_NAME%" rmdir /s /q "%TOMCAT_WEBAPPS%\%APP_NAME%"

REM Deploiement vers Tomcat
copy %APP_DIR%\%APP_NAME%.war "%TOMCAT_WEBAPPS%"

echo Deploiement termine. Redemarrez Tomcat si necessaire.
