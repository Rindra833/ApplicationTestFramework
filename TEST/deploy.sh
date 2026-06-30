#!/bin/bash

# Arrêter le script en cas d'erreur
set -e

APP_NAME="sprint02"

SRC_DIR="src/main/java"
WEB_DIR="src/main/webapp"
TEMP_DIR="TEMP"
LIB_DIR="lib"
APP_DIR="app"

# Adapter ce chemin selon ton installation Tomcat
TOMCAT_WEBAPPS="/home/rindra/Documents/tomcat/apache-tomcat-10.0.16/webapps/"

SERVLET_API_JAR="$LIB_DIR/jakarta.servlet-api-6.0.0.jar"
FRAMEWORK_JAR="$LIB_DIR/sprint-mvc-framework.jar"

# Vérification du framework
if [ ! -f "$FRAMEWORK_JAR" ]; then
    echo "Erreur : $FRAMEWORK_JAR introuvable."
    echo "Construisez d'abord le framework puis copiez son JAR dans $LIB_DIR."
    exit 1
fi

# Nettoyage
rm -rf "$TEMP_DIR"

mkdir -p "$TEMP_DIR/WEB-INF/classes"
mkdir -p "$TEMP_DIR/WEB-INF/lib"

# Compilation
if find "$SRC_DIR" -name "*.java" | grep -q .; then

    find "$SRC_DIR" -name "*.java" > sources.txt

    echo "Compilation en cours..."

    if ! javac \
        -cp "$SERVLET_API_JAR:$FRAMEWORK_JAR" \
        -d "$TEMP_DIR/WEB-INF/classes" \
        @sources.txt \
        2> compile_errors.txt
    then
        echo "Erreur de compilation !"
        cat compile_errors.txt
        cat sources.txt
        exit 1
    fi

    rm -f sources.txt
    rm -f compile_errors.txt

else
    echo "Aucun fichier Java trouvé dans $SRC_DIR."
fi

# Copier tous les fichiers du webapp sauf web.xml
cp -r "$WEB_DIR/"* "$TEMP_DIR/"

# Recréer WEB-INF
mkdir -p "$TEMP_DIR/WEB-INF"

# Copier web.xml
cp "$WEB_DIR/web.xml" "$TEMP_DIR/WEB-INF/web.xml"

# Copier les bibliothèques sauf Servlet API
for jar in "$LIB_DIR"/*.jar
do
    if [ "$(basename "$jar")" != "jakarta.servlet-api-6.0.0.jar" ]; then
        cp "$jar" "$TEMP_DIR/WEB-INF/lib/"
    fi
done

# Création du dossier app
mkdir -p "$APP_DIR"

# Création du WAR
cd "$TEMP_DIR"
jar -cvf "../$APP_DIR/$APP_NAME.war" .
cd ..

# Suppression de l'ancien déploiement
rm -f "$TOMCAT_WEBAPPS/$APP_NAME.war"
rm -rf "$TOMCAT_WEBAPPS/$APP_NAME"

# Déploiement
cp "$APP_DIR/$APP_NAME.war" "$TOMCAT_WEBAPPS"

echo
echo "====================================="
echo "Déploiement terminé avec succès !"
echo "WAR : $APP_DIR/$APP_NAME.war"
echo "Tomcat : $TOMCAT_WEBAPPS"
echo "====================================="