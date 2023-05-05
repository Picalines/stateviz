package com.statelang.compilation.result;

import lombok.Getter;

public final class JumpToIfNotInstruction extends Instruction {

    @Getter
    private final String destination;

    public JumpToIfNotInstruction(String destination) {
        super("jump_if_not");
        this.destination = destination;
    }
}
