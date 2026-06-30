#!/bin/bash

# Arrêter le script à la première erreur
set -e

FRAMEWORK_NAME="sprint-mvc-framework"

SRC_DIR="src/main/java"
BUILD_DIR="build/classes"
BUILD_JAR="build/${FRAMEWORK_NAME}.jar"
DIST_DIR="dist"
LIB_DIR="lib"
SERVLET_API_JAR="${LIB_DIR}/jakarta.servlet-api-6.0.0.jar"

# Vérification du JAR Servlet
if [ ! -f "$SERVLET_API_JAR" ]; then
    echo "Erreur : $SERVLET_API_JAR introuvable."
    echo "Copiez jakarta.servlet-api-6.0.0.jar dans le dossier lib du framework."
    exit 1
fi

# Nettoyage
rm -rf build
mkdir -p "$BUILD_DIR"
mkdir -p "$DIST_DIR"

# Génération de la liste des sources Java
find "$SRC_DIR" -name "*.java" > sources.txt

echo "Compilation du framework..."

if ! javac -cp "$SERVLET_API_JAR" \
    -d "$BUILD_DIR" \
    @sources.txt \
    2> compile_errors.txt
then
    echo "Erreur de compilation du framework !"
    cat compile_errors.txt
    rm -f sources.txt
    exit 1
fi

rm -f sources.txt
rm -f compile_errors.txt

echo "Création du JAR..."

rm -f "$BUILD_JAR"
rm -f "$DIST_DIR/$FRAMEWORK_NAME.jar"
rm -f "$FRAMEWORK_NAME.jar"

jar -cvf "$BUILD_JAR" -C "$BUILD_DIR" .

cp "$BUILD_JAR" "$DIST_DIR/$FRAMEWORK_NAME.jar"
cp "$DIST_DIR/$FRAMEWORK_NAME.jar" "$FRAMEWORK_NAME.jar"

echo
echo "======================================="
echo "Framework généré avec succès !"
echo "JAR : $DIST_DIR/$FRAMEWORK_NAME.jar"
echo "======================================="