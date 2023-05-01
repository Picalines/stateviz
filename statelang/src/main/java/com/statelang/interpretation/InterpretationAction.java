package com.statelang.interpretation;

import java.util.function.Consumer;

import lombok.Getter;

public final class InterpretationAction {

    public static final InterpretationAction INVALID = new InterpretationAction(c -> {
        throw new UnsupportedOperationException("invalid InterpretationAction");
    });

    @Getter
    private final Consumer<InterpretationContext> execute;

    @Getter
    private final String label;

    public InterpretationAction(String label, Consumer<InterpretationContext> execute) {
        this.execute = execute;
        this.label = label;
    }

    public InterpretationAction(Consumer<InterpretationContext> execute) {
        this(null, execute);
    }

    public InterpretationAction(String label) {
        this(label, c -> {});
    }
}
