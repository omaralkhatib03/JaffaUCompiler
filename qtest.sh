#!/bin/bash
DEBUG=$1
JAVA_ARGS='-ea -XX:+ShowCodeDetailsInExceptionMessages'
COMPILER_DIRS=./bin/:./src:./lib/antlr-4.13.0-complete.jar

make
echo "java $JAVA_ARGS -cp $COMPILER_DIRS Compiler -S ./tests/_example/example.c -o ./hi.txt $DEBUG"
java $JAVA_ARGS -cp $COMPILER_DIRS Compiler -S ./tests/_example/example.c -o ./hi.txt $DEBUG