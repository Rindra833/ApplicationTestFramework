#!/bin/bash

FRAMEWORK_NAME="framework"

SRC_DIR="src"
BUILD_DIR="build"
LIB_DIR="lib"

SERVLET_API="$LIB_DIR/servlet-api.jar"

rm -rf $BUILD_DIR

mkdir -p $BUILD_DIR

find $SRC_DIR -name "*.java" > sources.txt

javac \
-cp $SERVLET_API \
-d $BUILD_DIR \
@sources.txt

jar cvf $FRAMEWORK_NAME.jar \
-C $BUILD_DIR .

rm sources.txt

echo ""
echo "framework.jar créé"
echo ""