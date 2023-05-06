package com.statelang.compilation.instruction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExitInstruction extends Instruction {

    public static final ExitInstruction SUCCESS = new ExitInstruction(true);

    public static final ExitInstruction FAILURE = new ExitInstruction(false);

    @Getter
    private final boolean success;
}
