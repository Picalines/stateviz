package com.statelang.compilation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.base.Suppliers;
import com.statelang.ast.*;
import com.statelang.compilation.instruction.ExitInstruction;
import com.statelang.compilation.instruction.JumpToInstruction;
import com.statelang.compilation.instruction.StateInstruction;
import com.statelang.diagnostics.Report;
import com.statelang.diagnostics.Reporter;
import com.statelang.model.StateMachine;
import com.statelang.parsing.ProgramParser;
import com.statelang.tokenization.SourceSelection;
import com.statelang.tokenization.SourceText;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProgramCompiler {

    public static Optional<CompiledProgram> compile(Reporter reporter, Program program) {
        var stateMachineBuilder = StateMachine.builder();
        var programBuilder = CompiledProgram.builder();

        var compilationContext = CompilationContext.builder()
            .reporter(reporter)
            .stateMachineBuilder(stateMachineBuilder)
            .programBuilder(programBuilder)
            .build();

        var definitions = program.definitions();

        Map<Boolean, List<Definition>> partitionedDefinitions = definitions.stream()
            .collect(Collectors.partitioningBy(def -> def instanceof InStateDefinition));

        var inStateDefinitions = partitionedDefinitions.get(true);
        var nonInStateDefinitions = partitionedDefinitions.get(false);

        nonInStateDefinitions.forEach(def -> DefinitionCompiler.compile(compilationContext, def));

        var initialState = stateMachineBuilder.definedInitialState();
        if (initialState != null) {
            programBuilder
                .instruction(new StateInstruction(initialState))
                .instruction(new JumpToInstruction(initialState))
                .instruction(ExitInstruction.SUCCESS);
        }

        inStateDefinitions.forEach(def -> DefinitionCompiler.compile(compilationContext, def));

        if (!nonInStateDefinitions.stream().anyMatch(def -> def instanceof StateDefinition)) {
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

        programBuilder.stateMachine(stateMachine);
        return Optional.of(programBuilder.build());
    }

    public static Optional<CompiledProgram> compile(Reporter reporter, SourceText sourceText) {
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

        reachableStates.add(stateMachine.initialState());

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
