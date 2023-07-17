.text
.globl bsqrt

bsqrt:
addi sp, sp, -48
sw ra, 44(sp)
sw s0, 40(sp)
addi s0, sp, 48
sw a0, -20(s0)
sw a1, -24(s0)
sw a2, -28(s0)
_WHILE_2:
addi t0, s0, -20
lw t0, 0(t0)
li t1, 1
add t0, t0, t1
addi t1, s0, -24
lw t1, 0(t1)
slt t0, t0, t1
andi t0, t0, 255
beq t0, zero, _ENDWHILE_3
addi t1, s0, -20
lw t1, 0(t1)
addi t2, s0, -24
lw t2, 0(t2)
add t1, t1, t2
li t2, 1
sra t1, t1, t2
sw t1, -32(s0)
addi t1, s0, -32
lw t1, 0(t1)
addi t2, s0, -32
lw t2, 0(t2)
mul t1, t1, t2
sw t1, -36(s0)
addi t1, s0, -36
lw t1, 0(t1)
addi t2, s0, -28
lw t2, 0(t2)
sgt t1, t1, t2
xori t1, t1, 1
andi t1, t1, 255
beq t1, zero, _ELSE_6
addi t2, s0, -32
lw t2, 0(t2)
addi t3, s0, -20
sw t2, 0(t3)
j _ENDIF_5
_ELSE_6:
addi t0, s0, -32
lw t0, 0(t0)
addi t1, s0, -24
sw t0, 0(t1)
_ENDIF_5:
j _WHILE_2
_ENDWHILE_3:
addi t0, s0, -20
lw t0, 0(t0)
addi t1, s0, -20
lw t1, 0(t1)
mul t0, t0, t1
addi t1, s0, -28
lw t1, 0(t1)
slt t0, t0, t1
andi t0, t0, 255
beq t0, zero, _ELSE_10
addi t1, s0, -24
lw t1, 0(t1)
mv a0, t1
j _RETURN_0
j _ENDIF_9
_ELSE_10:
addi t0, s0, -20
lw t0, 0(t0)
mv a0, t0
j _RETURN_0
_ENDIF_9:
_RETURN_0:
lw ra, 44(sp)
lw s0, 40(sp)
addi sp, sp, 48
jr ra

