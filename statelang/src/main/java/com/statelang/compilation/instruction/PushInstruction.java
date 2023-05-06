package com.statelang.compilation.instruction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class PushInstruction extends Instruction {

    @Getter
    private final Object value;
}
