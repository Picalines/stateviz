package com.statelang.compilation;

import java.util.Optional;

import com.statelang.ast.Program;
import com.statelang.diagnostics.Report;
import com.statelang.diagnostics.Reporter;
import com.statelang.interpretation.Interpreter;
import com.statelang.model.StateMachine;
import com.statelang.tokenization.SourceSelection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProgramCompiler {

    public static Optional<Interpreter> compile(Reporter reporter, Program program) {
        var stateMachineBuilder = StateMachine.builder();
        var interpreterBuilder = Interpreter.builder();

        var compilationContext = new CompilationContext(reporter, stateMachineBuilder, interpreterBuilder);

        var definitions = program.definitions();

        definitions.stream().forEach(def -> DefinitionCompiler.compile(compilationContext, def));

        if (!stateMachineBuilder.hasDefinedStates()) {
            reporter.report(
                Report.builder()
                    .kind(Report.Kind.MISSING_STATE_DEFINITION)
                    .selection(SourceSelection.FIRST_CHARACTER)
            );
        }

        interpreterBuilder.stateMachine(stateMachineBuilder.build());
        return Optional.of(interpreterBuilder.build());
    }
}
