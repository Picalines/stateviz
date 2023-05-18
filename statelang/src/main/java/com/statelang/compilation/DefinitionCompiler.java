package com.statelang.compilation;

import java.util.Collections;
import java.util.HashSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.statelang.ast.Attribute;
import com.statelang.ast.ConstantDefinition;
import com.statelang.ast.Definition;
import com.statelang.ast.InStateDefinition;
import com.statelang.ast.StateDefinition;
import com.statelang.ast.VariableDefinition;
import com.statelang.compilation.instruction.JumpToInstruction;
import com.statelang.compilation.instruction.LabelInstruction;
import com.statelang.compilation.instruction.SourceLocationInstruction;
import com.statelang.compilation.instruction.StoreInstruction;
import com.statelang.compilation.symbol.ConstantSymbol;
import com.statelang.compilation.symbol.StateSymbol;
import com.statelang.compilation.symbol.VariableSymbol;
import com.statelang.diagnostics.Report;
import com.statelang.model.StateMachine;
import com.statelang.tokenization.SourceSelection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class DefinitionCompiler {

    public static void compile(CompilationContext context, Definition definition) {
        if (definition instanceof StateDefinition stateDefinition) {
            compileStateDefinition(context, stateDefinition);
            return;
        }

        if (definition instanceof VariableDefinition variableDefinition) {
            compileVariableDefinition(context, variableDefinition);
            return;
        }

        if (definition instanceof ConstantDefinition constantDefinition) {
            compileConstantDefinition(context, constantDefinition);
            return;
        }

        if (definition instanceof InStateDefinition inStateDefinition) {
            compileInStateDefinition(context, inStateDefinition);
            return;
        }

        throw new UnsupportedOperationException(definition.getClass().getName() + " definition is not implemented");
    }

    private static <T> Predicate<T> distinctByFilter(Function<T, ?> mapper) {
        var seen = new HashSet<>();
        return value -> seen.add(mapper.apply(value));
    }

    private static void compileStateDefinition(CompilationContext context, StateDefinition stateDefinition) {
        var stateMachineBuilder = context.stateMachineBuilder();
        var programBuilder = context.programBuilder();
        var states = stateDefinition.states();

        var stateNames = states.stream().map(StateDefinition.State::name).toList();

        states
            .stream()
            .filter(state -> Collections.frequency(stateNames, state.name()) > 1)
            .filter(distinctByFilter(StateDefinition.State::name))
            .forEach(state -> {
                context.reporter().report(
                    Report.builder()
                        .kind(Report.Kind.DUPLICATE_IDENTIFIER)
                        .selection(state.nameToken().selection())
                        .info(state.name())
                );
            });

        states = states
            .stream()
            .filter(distinctByFilter(StateDefinition.State::name))
            .toList();

        if (!stateMachineBuilder.definedStates().isEmpty()) {
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.AMBIGUOUS_DEFINITION)
                    .selection(stateDefinition.stateToken().selection())
            );
        }

        if (states.size() < 2) {
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.TOO_LITTLE_STATES)
                    .selection(stateDefinition.stateToken().selection())
            );
        }

        states.forEach(state -> {
            stateMachineBuilder.state(
                new StateMachine.State(
                    state.name(),
                    state.attributes()
                        .stream()
                        .collect(Collectors.toMap(Attribute::name, Attribute::value))
                )
            );

            programBuilder.symbol(new StateSymbol(state.name()));
        });

        if (!states.isEmpty()) {
            stateMachineBuilder.initialState(states.get(0).name());
        }
    }

    private static void compileInStateDefinition(CompilationContext context, InStateDefinition inStateDefinition) {
        var stateMachineBuilder = context.stateMachineBuilder();
        var state = inStateDefinition.state();

        var isStateDefined = stateMachineBuilder.definedStates()
            .stream()
            .anyMatch(definedState -> definedState.name().equals(state));

        if (!isStateDefined) {
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.UNDEFINED_STATE)
                    .selection(inStateDefinition.stateToken().selection())
                    .info(state)
            );
        }

        var programBuilder = context.programBuilder();

        if (programBuilder.hasDefinedLabel(state)) {
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.AMBIGUOUS_DEFINITION)
                    .selection(inStateDefinition.stateToken().selection())
            );
        } else {
            programBuilder
                .instruction(new LabelInstruction(state))
                .instruction(
                    new SourceLocationInstruction(inStateDefinition.stateToken().selection().start())
                );
        }

        context.currentState(state);
        StateActionCompiler.compile(context, inStateDefinition.actionBlock());
        context.currentState(null);

        programBuilder.instruction(new JumpToInstruction(state));

        if (isStateDefined && !context.transitioned()) {
            stateMachineBuilder.transition(state, state);
        }

        context.transitioned(false);
    }

    private static void compileVariableDefinition(CompilationContext context, VariableDefinition definition) {
        var variableName = definition.variableName();

        var isDuplicate = checkDuplicateSymbol(context, variableName, definition.variableNameToken().selection());

        var programBuilder = context.programBuilder();

        programBuilder.instruction(
            new SourceLocationInstruction(definition.initialVariableValue().selection().start())
        );

        var expressionType = ValueExpressionCompiler.compile(context, definition.initialVariableValue());

        if (!isDuplicate) {
            programBuilder.symbol(new VariableSymbol(variableName, expressionType));
        }

        programBuilder.instruction(new StoreInstruction(variableName));
    }

    private static void compileConstantDefinition(CompilationContext context, ConstantDefinition definition) {
        var constantName = definition.constantName();

        var isDuplicate = checkDuplicateSymbol(context, constantName, definition.constantNameToken().selection());

        var programBuilder = context.programBuilder();

        programBuilder.instruction(
            new SourceLocationInstruction(definition.initialConstantValue().selection().start())
        );

        var expressionType = ValueExpressionCompiler.compile(context, definition.initialConstantValue());

        if (!isDuplicate) {
            programBuilder.symbol(new ConstantSymbol(constantName, expressionType));
        }

        programBuilder.instruction(new StoreInstruction(constantName));
    }

    private static boolean checkDuplicateSymbol(
        CompilationContext context,
        String identifier,
        SourceSelection identifierSelection)
    {
        var symbols = context.programBuilder().definedSymbols();

        if (symbols.containsKey(identifier)) {
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.DUPLICATE_IDENTIFIER)
                    .selection(identifierSelection)
                    .info(identifier)
            );
            return true;
        }

        return false;
    }
}
