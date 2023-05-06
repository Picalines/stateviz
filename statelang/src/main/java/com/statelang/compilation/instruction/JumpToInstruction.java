package com.statelang.compilation.instruction;

import lombok.Getter;

public final class JumpToInstruction extends Instruction {

    @Getter
    private final String destination;

    public JumpToInstruction(String destination) {
        super("jump");
        this.destination = destination;
    }
}
