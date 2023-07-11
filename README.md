# JaffaUCompiler
JaffaUCompiler is an ANSI C to RISC-V compiler written in java. The compiler uses ANTLR 4.13.0 to create an AST which is then traversed using a Visitor class to generate code. The goal of this project was to learn java as well as demonstrate key ideas that should be taken into acount when using an OOP language. The compiler passes 30/88 tests at the moment. (This will be improved as more features get added). At the minute the compiler can evauluate only a subsection of integer operations. These include declaring variables, assigning to them and returning them from funcitons.

## Compiler Features

-[x] a file containing just a single function with no arguments

-[x] variables of `int` type

-[x] local variables

-[x] arithmetic and logical expressions

-[] if-then-else statements

-[] while loops

-[] files containing multiple functions that call each other

-[x] functions that take up to four parameter
-[] for loops

-[] arrays declared globally (i.e. outside of any function in your file)

-[] arrays declared locally (i.e. inside a function)

-[] reading and writing elements of an array

-[] recursive function calls

-[] the `enum` keyword

-[] `switch` statements

-[] the `break` and `continue` keywords

-[x] variables of `double`, `float`, `char`, `unsigned`, structs, and pointer types

-[] calling externally-defined functions (i.e. the file being compiled declares a function, but its
 definition is provided in a different file that is linked in later on)

-[] functions that take more than four parameters

-[] mutually recursive function calls

-[x] locally scoped variable declarations (e.g. a variable that is declared inside the body of a 
while loop, such as `while(...) { int x = ...; ... }`.

-[] the `typedef` keyword

-[] the `sizeof(...)` function (which takes either a type or a variable)

-[x] taking the address of a variable using the `&` operator

-[] dereferencing a pointer-variable using the `*` operator

-[] pointer arithmetic

-[] character literals, including escape sequences like `\n`

-[] strings (as NULL-terminated character arrays)

-[] declaration and use of structs























