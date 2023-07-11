# JaffaUCompiler

JaffaUCompiler is an ANSI C to RISC-V compiler written in java. The compiler uses ANTLR 4.13.0 to create an AST which is then traversed using a Visitor class to generate code. The goal of this project was to learn java as well as demonstrate key ideas that should be taken into acount when using an OOP language. The compiler passes 30/88 tests at the moment. (This will be improved as more features get added). At the minute the compiler can evauluate only a subsection of integer operations. These include declaring variables, assigning to them and returning them from funcitons.

## Compiler Java Features

The compiler has a few noticable java features which demonstrate some key learning points. 

### Java IO
The compiler reads in from CL the following format:

`java (SOME JAVA ARGUMENTS) bin/Compiler -s (Path to C source code) -o (Path to output) -v (Optional: To run compiler in verbose mode)`

The compiler reads in the C source code from a file and outputs a RISCV executable in the specified output path. The -v option allows the user to cun the compiler in verbose mode, where it will output a summary of what it compiled

### Inhertiance, Polymorphism and Encapsulation 
The compiler relies heavily on these three concepts. Inhertiance is used to reduce code redundancy and group similar classes together. For example the Symbols package contains a Top level class "CommonSymbol" which contains type, id and offset. This allows all other symbols which inhertic common symbol to also inherit these fields, hence the getters and setters for these fields only need to be defined once. 

Polymorphism allows the compiler to store all symbols in a single table. All symbols (Variables, Arrays, Pointers etc) can be referred to as a Common Symbol since they all inherit from it. Thus only a single Symbol table per scope is required to hold all of them. 

Note: Reading from the symbol table should be done carefully, hence it is not possible to treat a CommonSymbol which has just been read from the symbol table as specifc child class (Variable for example). To do so, the compiler first checks if the symbols is infact a specific child clas using `instanceof`, and then typecasts the class to the correct child class.

Encapsulation was used to seperate between the context, register manager and the Compiler class its self. This is important since the compiler should not have direct access to the registers and the symbols themselves. It should only be able to modify them based on the context. This is seen in the compiler since all relevant fields in the Context class are defined as private and are only accessed through public defined functions.
### Java Build System

In order to understand the Java Build system, a Make file was created. The make file compiles the java code into .class byte code which can then be interperted using the JVM. The Makefile demonstrated the use of external libraries (ANTLR 4.13.0). The basic system is as follows:
1. Generate .java files using the ANTLR Tool which requires the antlr jar to be added to class path
2. Add 'package grammar;' to the beginning of the java files to allow the Compiler to import them as a package
3. (Optional) Compile the grammar files seperatley 
(This target is commented in the make file as it is unnecessary)
4. Compiler the Compiler using javac (Requires adding some more dirs into the class path)
5. Run the Compiler.class in the bin directory (Requires adding dirs to classpath as well, see `qtest.sh`)

## Portable

The compiler was also made to demonstrate how portable java is. The compiler does not require the user to download ANTLR or have it on their system. However the main reason as to why java is portable is that it is platform independent. Hence the JVM is what interperts byte code found in .class files, unlike other programming languages where the compilation system depends on some aspect of the OS its running on.

The only requirments are: 
* Java version 11+
* Run the MakeFile and shell files in a bash terminal, not a windows terminal. (For Windows use cygwin or WSL)
* ./qtest.sh should be run on a bash terminal, (i think replacing java with java.exe should allow you to run it on windows terminal)

## Scipts 
* ./test.sh should be run in the docker container provided (Docker container might take a while to build)
* ./qtest.sh by defualt will compiler the source coude in ./compiler_tests/_example/example.c 

## What Features of ANSI C the Compiler can compile
- [x] a file containing just a single function with no arguments
- [x] variables of `int` type
- [x] local variables
- [x] arithmetic and logical expressions
- if-then-else statements
- while loops
- files containing multiple functions that call each other
- [x] functions that take up to four parameters
- for loops
- arrays declared globally (i.e. outside of any function in your file)
- arrays declared locally (i.e. inside a function)
- reading and writing elements of an array
- recursive function calls
- the `enum` keyword
- `switch` statements
- the `break` and `continue` keywords
- [x] variables of `double`, `float`, `char`, `unsigned`, structs, and pointer types
- calling externally-defined functions (i.e. the file being compiled declares a function, but its definition is provided in a different file that is linked in later on)
- functions that take more than four parameters
- mutually recursive function calls
- [x] locally scoped variable declarations (e.g. a variable that is declared inside the body of a while loop, such as `while(...) { int x = ...; ... }`.)
- the `typedef` keyword
- the `sizeof(...)` function (which takes either a type or a variable)
- [x] taking the address of a variable using the `&` operator
- dereferencing a pointer-variable using the `*` operator
- pointer arithmetic
- character literals, including escape sequences like `\n`
- strings (as NULL-terminated character arrays)
- declaration and use of structs























