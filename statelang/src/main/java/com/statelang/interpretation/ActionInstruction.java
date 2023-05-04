package com.statelang.interpretation;

@FunctionalInterface
public non-sealed interface ActionInstruction extends Instruction {
    void execute(InterpretationContext context);
}
