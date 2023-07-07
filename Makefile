ANTLR_LIB=".:./lib/antlr-4.13.0-complete.jar"
COMPILER_PATH=".:./lib/antlr-4.13.0-complete.jar:./src"
JAVA_ARGS=-XX:+ShowCodeDetailsInExceptionMessages
JAVAC_ARGS=-Xdiags:verbose
# COMPILER_DIRS=./src/:./:./lib/antlr-4.13.0-complete.jar
OUTPUT_DIR=./bin

src/Compiler.class: src/grammar/c*.class
	javac $(JAVAC_ARGS) -d $(OUTPUT_DIR) --class-path $(COMPILER_PATH) src/Compiler.java

src/grammar/c*.class: src/grammar/c*.java
	javac $(JAVAC_ARGS) --class-path $(ANTLR_LIB) src/grammar/*.java

src/grammar/c*.java: src/grammar/c.g4 
	java $(JAVA_ARGS) -classpath $(ANTLR_LIB) org.antlr.v4.Tool -visitor src/grammar/c.g4 
	
.PHONY: clean grammar antlr

src/grammar: src/grammar/c*.class

antlr: src/grammar/c*.java


clean:
# rm -rf grammar/*.java
	rm -rf src/grammar/*.interp
	rm -rf src/grammar/*.tokens
	rm -rf src/grammar/*.class
	rm -rf src/*.class
# rm -rf bin/*.class
	rm -rf bin/*
