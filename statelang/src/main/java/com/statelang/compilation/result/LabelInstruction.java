package com.statelang.compilation.result;

import lombok.Getter;

public final class LabelInstruction extends Instruction {

    @Getter
    private final String label;

    public LabelInstruction(String label) {
        super("label");
        this.label = label;
    }
}
