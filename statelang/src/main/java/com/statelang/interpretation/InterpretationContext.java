package com.statelang.interpretation;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public final class InterpretationContext {

    private final Supplier<String> getState;

    private final Stack<Object> stack = new Stack<>();

    private final Map<String, Object> namedValues = new HashMap<>();

    public String state() {
        return getState.get();
    }

    public RuntimeException performTransition(String newState) {
        throw new InterpreterTransitionException(newState);
    }

    public RuntimeException jumpTo(String label) {
        throw new InterpreterJumpException(label);
    }

    public RuntimeException exit(InterpreterExitReason reason) {
        throw new InterpreterExitException(reason);
    }
}
