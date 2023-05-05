package com.statelang.compilation.result;

import lombok.Getter;

public final class ExitInstruction extends Instruction {

    public static final ExitInstruction SUCCESS = new ExitInstruction(true);

    public static final ExitInstruction FAILURE = new ExitInstruction(false);

    @Getter
    private final boolean success;

    private ExitInstruction(boolean success) {
        super("exit");
        this.success = success;
    }
}
