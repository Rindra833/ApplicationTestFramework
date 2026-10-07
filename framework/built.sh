#!/bin/bash

set -e

FRAMEWORK_NAME="sprint-mvc-framework"

SRC_DIR="src/main/java"
BUILD_DIR="build/classes"
BUILD_JAR="build/${FRAMEWORK_NAME}.jar"

DIST_DIR="dist"
LIB_DIR="lib"

SERVLET_API_JAR="${LIB_DIR}/jakarta.servlet-api-6.0.0.jar"

TEMP="$(pwd)/build"
TMP="$(pwd)/build"

# Vérifier la présence du JAR Servlet
if [ ! -f "$SERVLET_API_JAR" ]; then
    echo "Erreur : $SERVLET_API_JAR introuvable."
    echo "Copiez jakarta.servlet-api-6.0.0.jar dans le dossier lib du framework."
    exit 1
fi

# Nettoyage du dossier build
if [ -d "build" ]; then
    rm -rf build
fi

# Création des dossiers nécessaires
mkdir -p "$BUILD_DIR"
mkdir -p "$DIST_DIR"

# Générer la liste des fichiers Java
rm -f sources.txt

find "$SRC_DIR" -type f -name "*.java" > sources.txt

echo "Compilation du framework..."

if ! javac -cp "$SERVLET_API_JAR" -d "$BUILD_DIR" @sources.txt 2> compile_errors.txt; then
    echo "Erreur de compilation du framework !"
    cat compile_errors.txt
    rm -f sources.txt
    exit 1
fi

rm -f sources.txt
rm -f compile_errors.txt

echo "Création du JAR..."

# Supprimer les anciens JAR
if [ -f "$DIST_DIR/${FRAMEWORK_NAME}.jar" ]; then
    rm -f "$DIST_DIR/${FRAMEWORK_NAME}.jar"
fi

if [ -f "${FRAMEWORK_NAME}.jar" ]; then
    rm -f "${FRAMEWORK_NAME}.jar"
fi

if [ -f "$BUILD_JAR" ]; then
    rm -f "$BUILD_JAR"
fi

# Création du JAR
jar -cvf "$BUILD_JAR" -C "$BUILD_DIR" .

if [ $? -ne 0 ]; then
    echo "Erreur de création du JAR du framework !"
    exit 1
fi

# Copie dans dist
cp "$BUILD_JAR" "$DIST_DIR/${FRAMEWORK_NAME}.jar"

if [ $? -ne 0 ]; then
    echo "Erreur de copie du JAR dans le dossier dist !"
    exit 1
fi

# Copie à la racine du framework
cp "$DIST_DIR/${FRAMEWORK_NAME}.jar" "${FRAMEWORK_NAME}.jar"

if [ $? -ne 0 ]; then
    echo "Erreur de copie du JAR à la racine du framework !"
    exit 1
fi

echo ""
echo "=========================================="
echo "Framework généré avec succès !"
echo "=========================================="
echo "JAR : $DIST_DIR/${FRAMEWORK_NAME}.jar"

