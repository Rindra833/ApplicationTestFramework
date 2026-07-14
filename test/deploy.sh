#!/bin/bash

# Définition des variables
APP_NAME="Sprint05"
SRC_DIR="src/main/java"
# Dans ton projet les JSP et le web.xml sont dans src/main/webapps
WEB_DIR="src/main/webapps"
BUILD_DIR="build"
LIB_DIR="lib"
TOMCAT_WEBAPPS="/home/rindra/Documents/tomcat/apache-tomcat-10.0.16/webapps"
SERVLET_API_JAR=$(echo lib/*.jar | tr ' ' ':')

# Nettoyage et création du répertoire temporaire
# rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/WEB-INF/classes"
mkdir -p "$BUILD_DIR/WEB-INF/lib"

# Compilation des fichiers Java avec le JAR des Servlets
find "$SRC_DIR" -name "*.java" > sources.txt
javac -cp "$SERVLET_API_JAR" -d "$BUILD_DIR/WEB-INF/classes" @sources.txt
rm -f sources.txt

# Copier les fichiers web (web.xml, JSP, etc.)
cp -r "$WEB_DIR/"* "$BUILD_DIR/"
# On déplace le web.xml à sa place standard dans WEB-INF
if [ -f "$BUILD_DIR/web.xml" ]; then
  mv -f "$BUILD_DIR/web.xml" "$BUILD_DIR/WEB-INF/web.xml"
fi

# Copier les librairies nécessaires dans WEB-INF/lib (optionnel mais recommandé)
cp -f "$LIB_DIR"/*.jar "$BUILD_DIR/WEB-INF/lib/" 2>/dev/null || true

# Générer le fichier .war dans le dossier build
cd "$BUILD_DIR" || exit 
jar -cvf "$APP_NAME.war" *
cd ..

# Déploiement dans Tomcat
cp -f "$BUILD_DIR/$APP_NAME.war" "$TOMCAT_WEBAPPS/"

echo ""
echo "Déploiement terminé. Copie de $APP_NAME.war dans $TOMCAT_WEBAPPS."
echo "Redémarrez Tomcat si nécessaire."
echo ""
