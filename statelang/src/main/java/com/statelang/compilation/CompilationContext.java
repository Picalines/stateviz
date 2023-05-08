package com.statelang.compilation;

import com.statelang.diagnostics.Reporter;
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
    @Setter
    @Builder.Default
    private boolean transitioned = false;
}
