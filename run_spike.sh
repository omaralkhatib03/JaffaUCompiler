#!/bin/bash

DEBUG=$1
SRCFILE='compiler_tests/_example/example.c'
OUT='test/out.s'
DRIVER='compiler_tests/_example/example_driver.c'
JAVA_ARGS='-ea -XX:+ShowCodeDetailsInExceptionMessages'
COMPILER_DIRS=./bin/:./src:./lib/antlr-4.13.0-complete.jar

echo $SRCFILE
echo $OUT
echo $DRIVER


make 
echo 'java $JAVA_ARGS -cp $COMPILER_DIRS Compiler -S ./compiler_tests/_example/example.c -o ./hi.txt $DEBUG'
java $JAVA_ARGS -cp $COMPILER_DIRS Compiler -S $SRCFILE -o $OUT -v
riscv64-unknown-elf-gcc -march=rv32imfd -mabi=ilp32d -o "test/out" -c $OUT
riscv64-unknown-elf-gcc -march=rv32imfd -mabi=ilp32d -static -o "test/complete" "test/out" $DRIVER
riscv64-unknown-elf-objdump -d test/complete > bin/disass.dump
spike $DEBUG pk "test/complete"
