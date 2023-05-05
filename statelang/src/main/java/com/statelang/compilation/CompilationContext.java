package com.statelang.compilation;

import java.util.HashMap;
import java.util.Map;

import com.statelang.diagnostics.Reporter;
import com.statelang.model.InstanceType;
import com.statelang.model.StateMachine;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
final class CompilationContext {

    @Getter
    private final Reporter reporter;

    @Getter
    private final StateMachine.StateMachineBuilder stateMachineBuilder;

    @Getter
    private final CompiledProgram.CompiledProgramBuilder programBuilder;

    @Getter
    @Setter
    @Builder.Default
    private String currentState = null;

    @Getter
    private final Map<String, InstanceType<?>> constants = new HashMap<>();

    @Getter
    private final Map<String, InstanceType<?>> variables = new HashMap<>();
}
