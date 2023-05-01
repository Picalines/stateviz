package com.statelang.compilation;

import java.util.HashMap;
import java.util.Map;

import com.statelang.diagnostics.Reporter;
import com.statelang.interpretation.Interpreter;
import com.statelang.model.InstanceType;
import com.statelang.model.StateMachine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class CompilationContext {

    @Getter
    private final Reporter reporter;

    @Getter
    private final StateMachine.StateMachineBuilder stateMachineBuilder;

    @Getter
    private final Interpreter.InterpreterBuilder interpreterBuilder;

    @Getter
    private String currentState = null;

    @Getter
    private Map<String, InstanceType<?>> constants = new HashMap<>();

    @Getter
    private Map<String, InstanceType<?>> variables = new HashMap<>();

    private CompilationContext(
        Reporter reporter,
        StateMachine.StateMachineBuilder stateMachineBuilder,
        Interpreter.InterpreterBuilder interpreterBuilder,
        String currentState,
        Map<String, InstanceType<?>> constants,
        Map<String, InstanceType<?>> variables)
    {
        this.reporter = reporter;
        this.stateMachineBuilder = stateMachineBuilder;
        this.interpreterBuilder = interpreterBuilder;
        this.currentState = currentState;
        this.constants = constants;
        this.variables = variables;
    }

    public CompilationContext withCurrentState(String newCurrentState) {
        return currentState == newCurrentState
            ? this
            : new CompilationContext(
                reporter,
                stateMachineBuilder,
                interpreterBuilder,
                newCurrentState,
                constants,
                variables
            );
    }
}
