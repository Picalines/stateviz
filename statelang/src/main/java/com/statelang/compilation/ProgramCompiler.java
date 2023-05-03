package com.statelang.compilation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.statelang.ast.Definition;
import com.statelang.ast.InStateDefinition;
import com.statelang.ast.Program;
import com.statelang.diagnostics.Report;
import com.statelang.diagnostics.Reporter;
import com.statelang.interpretation.InterpretationAction;
import com.statelang.interpretation.Interpreter;
import com.statelang.interpretation.InterpreterExitReason;
import com.statelang.model.StateMachine;
import com.statelang.parsing.ProgramParser;
import com.statelang.tokenization.SourceSelection;
import com.statelang.tokenization.SourceText;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProgramCompiler {

    public static Optional<Interpreter> compile(Reporter reporter, Program program) {
        var stateMachineBuilder = StateMachine.builder();
        var interpreterBuilder = Interpreter.builder();

        var compilationContext = new CompilationContext(reporter, stateMachineBuilder, interpreterBuilder);

        var definitions = program.definitions();

        Map<Boolean, List<Definition>> partitionedDefinitions = definitions.stream()
            .collect(Collectors.partitioningBy(def -> def instanceof InStateDefinition));

        var stateDefinitions = partitionedDefinitions.get(true);
        var nonStateDefinitions = partitionedDefinitions.get(false);

        nonStateDefinitions.forEach(def -> DefinitionCompiler.compile(compilationContext, def));

        interpreterBuilder
            .stateAction(new InterpretationAction(Interpreter.SELECT_BRANCH_LABEL))
            .stateAction(new InterpretationAction(c -> {
                c.jumpTo(c.state());
            }))
            .stateAction(new InterpretationAction(c -> {
                c.jumpTo(Interpreter.EXIT_LABEL);
            }));

        stateDefinitions.forEach(def -> DefinitionCompiler.compile(compilationContext, def));

        interpreterBuilder.stateAction(new InterpretationAction(Interpreter.EXIT_LABEL, c -> {
            c.exit(InterpreterExitReason.FINAL_STATE_REACHED);
        }));

        if (stateMachineBuilder.definedStates().isEmpty()) {
            reporter.report(
                Report.builder()
                    .kind(Report.Kind.MISSING_STATE_DEFINITION)
                    .selection(SourceSelection.FIRST_CHARACTER)
            );
        }

        if (reporter.hasErrors()) {
            return Optional.empty();
        }

        interpreterBuilder.stateMachine(stateMachineBuilder.build());
        return Optional.of(interpreterBuilder.build());
    }

    public static Optional<Interpreter> compile(Reporter reporter, SourceText sourceText) {
        var program = ProgramParser.program.tryParse(sourceText, reporter);
        return program.flatMap(programTree -> compile(reporter, programTree));
    }
}
