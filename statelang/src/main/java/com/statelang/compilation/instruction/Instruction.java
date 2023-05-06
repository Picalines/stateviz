package com.statelang.compilation.instruction;

import lombok.Getter;

public abstract class Instruction {

    @Getter
    private final String name;

    Instruction(String name) {
        this.name = name;
    }
}
