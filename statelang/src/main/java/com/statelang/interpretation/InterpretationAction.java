package com.statelang.interpretation;

public interface InterpretationAction {

    public static final InterpretationAction EMPTY = c -> {};

    void execute(InterpretationContext context);

    default InterpretationAction andThen(InterpretationAction next) {
        return context -> {
            execute(context);
            next.execute(context);
        };
    }
}
