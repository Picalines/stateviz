package com.statelang.compilation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.base.Suppliers;
import com.statelang.ast.Definition;
import com.statelang.ast.InStateDefinition;
import com.statelang.ast.Program;
import com.statelang.ast.StateDefinition;
import com.statelang.diagnostics.Report;
import com.statelang.diagnostics.Reporter;
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

    static final String EXIT_LABEL = "$exit$";

    static final String SELECT_BRANCH_LABEL = "$select_branch$";

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
            .jumpLabel(SELECT_BRANCH_LABEL)
            .instruction(c -> c.jumpTo(c.state()))
            .instruction(c -> c.jumpTo(EXIT_LABEL));

        stateDefinitions.forEach(def -> DefinitionCompiler.compile(compilationContext, def));

        interpreterBuilder
            .jumpLabel(EXIT_LABEL)
            .instruction(c -> c.exit(InterpreterExitReason.FINAL_STATE_REACHED));

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

        var stateMachine = stateMachineBuilder.build();

        warnUnreachableStates(reporter, definitions, stateMachine);

        interpreterBuilder.stateMachine(stateMachine);
        return Optional.of(interpreterBuilder.build());
    }

    public static Optional<Interpreter> compile(Reporter reporter, SourceText sourceText) {
        var program = ProgramParser.program.tryParse(sourceText, reporter);
        return program.flatMap(programTree -> compile(reporter, programTree));
    }

    private static void warnUnreachableStates(
        Reporter reporter,
        List<Definition> definitions,
        StateMachine stateMachine)
    {
        var reachableStates = stateMachine
            .transitions()
            .values()
            .stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        reachableStates.add(stateMachine.state());

        var unreachableStates = stateMachine
            .states()
            .stream()
            .filter(Predicate.not(reachableStates::contains));

        var stateDefinition = Suppliers.memoize(
            () -> definitions
                .stream()
                .filter(def -> def instanceof StateDefinition)
                .map(def -> (StateDefinition) def)
                .findFirst()
                .get()
        );

        unreachableStates.forEach(unreachableState -> {
            reporter.report(
                Report.builder()
                    .kind(Report.Kind.UNREACHABLE_STATE)
                    .selection(stateDefinition.get().stateToken().selection())
                    .info(unreachableState)
            );
        });
    }
}
