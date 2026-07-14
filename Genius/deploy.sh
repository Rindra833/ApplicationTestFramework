#!/bin/bash
APP_NAME="Genius"
SRC_DIR="src/main/java"
WEB_DIR="src/main/webapp"
BUILD_DIR="build"
LIB_DIR="lib"
SERVLET_API_JAR="$LIB_DIR/servlet-api.jar"
APP_TEST_WEBAPPS="/home/rindra/Documents/sprint5/test/lib"

# Nettoyage et création du répertoire temporaire
rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR/classes
mkdir -p $BUILD_DIR/lib

# Compilation des fichiers Java avec le JAR des Servlets
find $SRC_DIR -name "*.java" > sources.txt
javac -cp ".:lib/*" -d $BUILD_DIR/classes @sources.txt 
rm sources.txt

# Copier les jars de dépendances vers build/lib (reste inchangé)
cp -r $LIB_DIR/*.jar $BUILD_DIR/lib/ 2>/dev/null || true

# Générer le fichier .jar contenant uniquement les classes du framework (ne pas inclure les jars de lib à l'intérieur)
cd $BUILD_DIR || exit
jar -cvf $APP_NAME.jar -C classes .
cd ..

# Déploiement dans le lib de l'application de test
mkdir -p $APP_TEST_WEBAPPS

# Copier le jar du framework
cp -f $BUILD_DIR/$APP_NAME.jar $APP_TEST_WEBAPPS

# Copier les dépendances séparément dans WEB-INF/lib (sauf servlet-api.jar fourni par Tomcat)
for j in $LIB_DIR/*.jar; do
  if [ -f "$j" ]; then
    base=$(basename "$j")
    if [[ "$base" != "servlet-api.jar" && "$base" != "jakarta.servlet-api.jar" ]]; then
      cp -f "$j" "$APP_TEST_WEBAPPS"
    fi
  fi
done

echo "Framework déployé dans $APP_TEST_WEBAPPS :"
ls -1 $APP_TEST_WEBAPPS || true