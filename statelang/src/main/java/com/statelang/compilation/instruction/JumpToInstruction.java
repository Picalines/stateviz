package com.statelang.compilation.instruction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class JumpToInstruction extends Instruction {

    @Getter
    private final String destination;
}
