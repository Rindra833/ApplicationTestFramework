#!/bin/bash

APP_NAME="ApplicationTest"

BUILD_DIR="build"

WEB_DIR="src/main/webapp"

TOMCAT_WEBAPPS="/home/rindra/Documents/tomcat/apache-tomcat-10.0.16/webapps"

rm -rf $BUILD_DIR

mkdir -p $BUILD_DIR

cp -r $WEB_DIR/* $BUILD_DIR/

cd $BUILD_DIR || exit

jar -cvf $APP_NAME.war *

cd ..

cp -f $BUILD_DIR/$APP_NAME.war \
$TOMCAT_WEBAPPS/

echo "Déploiement terminé"