#!/bin/bash
JAVA_ARGS=-XX:+ShowCodeDetailsInExceptionMessages
COMPILER_DIRS=./bin/:./src:./lib/antlr-4.13.0-complete.jar

make
java $JAVA_ARGS -cp $COMPILER_DIRS Compiler -S ./compiler_tests/_example/example.c -o ./hi.txt