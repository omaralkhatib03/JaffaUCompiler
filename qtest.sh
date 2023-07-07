#!/bin/bash
JAVA_ARGS=-XX:+ShowCodeDetailsInExceptionMessages
COMPILER_DIRS=./bin/:./:./lib/antlr-4.13.0-complete.jar


java $JAVA_ARGS -cp $COMPILER_DIRS Compiler