.text
.globl f

f:
addi sp, sp, -48
sw ra, 44(sp)
sw s0, 40(sp)
addi s0, sp, 48
fsw fa0, -20(s0)
sw a0, -24(s0)
lui t0, %hi(_FLOAT_2)
add t0, t0, %lo(_FLOAT_2)
flw ft0, 0(t0)
li t0, 0
sw t0, -32(s0)
_WHILE_3:
addi t0, s0, -32
lw t0, 0(t0)
addi t1, s0, -24
lw t1, 0(t1)
slt t0, t0, t1
andi t0, t0, 255
beq t0, zero, _ENDWHILE_4
addi t1, s0, -32
lw t1, 0(t1)
addi t1, t1, 1
addi t2, s0, -32
sw t1, 0(t2)
addi t0, s0, -28
flw ft0, 0(t0)
addi t0, s0, -20
flw ft1, 0(t0)
fmul.s ft0, ft0, ft1
addi t0, s0, -28
fsw ft0, 0(t0)
j _WHILE_3
_ENDWHILE_4:
addi t0, s0, -28
flw ft0, 0(t0)
fmv.s fa0, ft0
j _RETURN_0
_RETURN_0:
lw ra, 44(sp)
lw s0, 40(sp)
addi sp, sp, 48
jr ra

_FLOAT_2: .word 1065353216
