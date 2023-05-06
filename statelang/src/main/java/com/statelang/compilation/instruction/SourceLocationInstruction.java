package com.statelang.compilation.instruction;

import com.statelang.tokenization.SourceLocation;

import lombok.Getter;

public final class SourceLocationInstruction extends Instruction {

    @Getter
    private final SourceLocation location;

    public SourceLocationInstruction(SourceLocation location) {
        super("location");
        this.location = location;
    }
}
