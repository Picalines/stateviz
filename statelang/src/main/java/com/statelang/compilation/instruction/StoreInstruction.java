package com.statelang.compilation.instruction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StoreInstruction extends Instruction {

    @Getter
    private final String memoryKey;
}
