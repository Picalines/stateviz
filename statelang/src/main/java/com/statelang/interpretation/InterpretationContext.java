package com.statelang.interpretation;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.statelang.model.StateMachine;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public final class InterpretationContext {

    private final StateMachine stateMachine;

    private final Stack<Object> stack = new Stack<>();

    private final Map<String, Object> namedValues = new HashMap<>();

    private boolean stopped = false;

    public void stop() {
        stopped = true;
    }
}
