package com.statelang.compilation.instruction;

import lombok.Getter;

public final class LoadInstruction extends Instruction {

    @Getter
    private final String memoryKey;

    public LoadInstruction(String memoryKey) {
        super("load");
        this.memoryKey = memoryKey;
    }
}
