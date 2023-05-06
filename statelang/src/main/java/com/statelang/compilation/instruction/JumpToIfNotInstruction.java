package com.statelang.compilation.instruction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class JumpToIfNotInstruction extends Instruction {

    @Getter
    private final String destination;
}
