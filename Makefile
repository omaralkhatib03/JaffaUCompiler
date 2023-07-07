ANTLR_LIB=".:./lib/antlr-4.13.0-complete.jar"
SRC_PATH=".:./lib/antlr-4.13.0-complete.jar"
JAVA_ARGS=-XX:+ShowCodeDetailsInExceptionMessages
# COMPILER_DIRS=./src/:./:./lib/antlr-4.13.0-complete.jar
OUTPUT_DIR=./bin

src/Compiler.class: grammar/c*.class
	javac -d $(OUTPUT_DIR) --class-path $(ANTLR_LIB) src/Compiler.java
# java $(JAVA_ARGS) -cp $(COMPILER_DIRS) Compiler

grammar/c*.class: grammar/c*.java
	javac --class-path $(ANTLR_LIB) grammar/*.java

grammar/c*.java: grammar/c.g4 
	java $(JAVA_ARGS) -classpath $(ANTLR_LIB) org.antlr.v4.Tool -visitor grammar/c.g4 
	
.PHONY: clean grammar antlr

grammar: grammar/c*.class

antlr: grammar/c*.java


clean:
# rm -rf grammar/*.java
	rm -rf grammar/*.interp
	rm -rf grammar/*.tokens
	rm -rf grammar/*.class
	rm -rf src/*.class
	rm -rf bin/*.class
