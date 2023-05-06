package com.statelang.compilation.instruction;

import lombok.Getter;

public class StoreInstruction extends Instruction {

    @Getter
    private final String memoryKey;

    public StoreInstruction(String memoryKey) {
        super("store");
        this.memoryKey = memoryKey;
    }
}
