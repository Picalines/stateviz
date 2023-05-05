package com.statelang.compilation.result;

import lombok.Getter;

public final class PushInstruction extends Instruction {

    @Getter
    private final Object value;

    public PushInstruction(Object value) {
        super("push");
        this.value = value;
    }
}
