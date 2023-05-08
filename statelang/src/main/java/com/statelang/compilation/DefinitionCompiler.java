package com.statelang.compilation;

import java.util.Collections;

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

    private static void compileStateDefinition(CompilationContext context, StateDefinition stateDefinition) {
        var stateMachineBuilder = context.stateMachineBuilder();
        var programBuilder = context.programBuilder();
        var states = stateDefinition.states();

        states
            .stream()
            .filter(state -> Collections.frequency(stateDefinition.states(), state) > 1)
            .distinct()
            .forEach(state -> {
                context.reporter().report(
                    Report.builder()
                        .kind(Report.Kind.DUPLICATE_IDENTIFIER)
                        .selection(stateDefinition.stateToken().selection())
                        .info(state)
                );
            });

        states = states
            .stream()
            .distinct()
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
            stateMachineBuilder.state(state);
            programBuilder.symbol(new StateSymbol(state));
        });

        if (!states.isEmpty()) {
            stateMachineBuilder.initialState(states.get(0));
        }
    }

    private static void compileInStateDefinition(CompilationContext context, InStateDefinition inStateDefinition) {
        var stateMachineBuilder = context.stateMachineBuilder();
        var state = inStateDefinition.state();

        var isStateDefined = stateMachineBuilder.definedStates().contains(state);

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

        checkDuplicateSymbol(context, variableName, definition.variableNameToken().selection());

        var programBuilder = context.programBuilder();

        programBuilder.instruction(
            new SourceLocationInstruction(definition.initialVariableValue().selection().start())
        );

        var expressionType = ValueExpressionCompiler.compile(context, definition.initialVariableValue());

        programBuilder
            .symbol(new VariableSymbol(variableName, expressionType))
            .instruction(new StoreInstruction(variableName));
    }

    private static void compileConstantDefinition(CompilationContext context, ConstantDefinition definition) {
        var constantName = definition.constantName();

        checkDuplicateSymbol(context, constantName, definition.constantNameToken().selection());

        var programBuilder = context.programBuilder();

        programBuilder.instruction(
            new SourceLocationInstruction(definition.initialConstantValue().selection().start())
        );

        var expressionType = ValueExpressionCompiler.compile(context, definition.initialConstantValue());

        programBuilder
            .symbol(new ConstantSymbol(constantName, expressionType))
            .instruction(new StoreInstruction(constantName));
    }

    private static void checkDuplicateSymbol(
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
        }
    }
}
