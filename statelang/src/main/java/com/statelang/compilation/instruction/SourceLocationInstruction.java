package com.statelang.compilation.instruction;

import com.statelang.tokenization.SourceLocation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class SourceLocationInstruction extends Instruction {

    @Getter
    private final SourceLocation location;
}
