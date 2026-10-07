#!/bin/bash
set -e

APP_NAME="sprint6"
SRC_DIR="src"
WEB_DIR="WebContent"
BUILD_DIR="build"
LIB_DIR="WebContent/WEB-INF/lib"
TOMCAT_WEBAPPS="${CATALINA_HOME}/webapps"

# Nettoyage
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/WEB-INF/classes"
mkdir -p "$BUILD_DIR/WEB-INF/lib"
mkdir -p "$BUILD_DIR/WEB-INF"

# Compilation
echo ">>> Compilation..."
CLASSPATH="$(echo $LIB_DIR/*.jar | tr ' ' ':')"
find "$SRC_DIR" -name "*.java" > sources.txt
javac -cp "$CLASSPATH" -d "$BUILD_DIR/WEB-INF/classes" @sources.txt
rm -f sources.txt

# Copie des ressources web
cp -r "$WEB_DIR"/* "$BUILD_DIR/"

# Copie des libs (sauf servlet-api si fourni par Tomcat)
for j in "$LIB_DIR"/*.jar; do
  base=$(basename "$j")
  if [[ "$base" != "jakarta.servlet-api-6.0.0.jar" ]]; then
    cp -f "$j" "$BUILD_DIR/WEB-INF/lib/"
  fi
done

# WAR
echo ">>> Creation du WAR..."
cd "$BUILD_DIR"
jar -cf "../dist-${APP_NAME}.war" .
cd ..
mkdir -p dist
mv "dist-${APP_NAME}.war" "dist/${APP_NAME}.war"

# Déploiement
if [ -n "$TOMCAT_WEBAPPS" ] && [ -d "$TOMCAT_WEBAPPS" ]; then
  cp -f "dist/${APP_NAME}.war" "$TOMCAT_WEBAPPS/"
  echo ">>> Deploye dans $TOMCAT_WEBAPPS"
fi

echo ""
echo "WAR genere : dist/${APP_NAME}.war"