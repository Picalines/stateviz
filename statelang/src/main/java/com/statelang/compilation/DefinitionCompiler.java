package com.statelang.compilation;

import java.util.stream.Stream;

import com.statelang.ast.ConstantDefinition;
import com.statelang.ast.Definition;
import com.statelang.ast.InStateDefinition;
import com.statelang.ast.StateDefinition;
import com.statelang.ast.VariableDefinition;
import com.statelang.compilation.result.JumpToInstruction;
import com.statelang.compilation.result.LabelInstruction;
import com.statelang.compilation.result.SourceLocationInstruction;
import com.statelang.compilation.result.StoreInstruction;
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
        var states = stateDefinition.states();

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

        states.forEach(stateMachineBuilder::state);

        stateMachineBuilder.initialState(states.get(0));
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
            );
        }

        var programBuilder = context.programBuilder();

        if (programBuilder.hasDefinedLabel(state)) {
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.AMBIGUOUS_DEFINITION)
                    .selection(inStateDefinition.stateToken().selection())
            );
        }

        programBuilder
            .instruction(new LabelInstruction(state))
            .instruction(
                new SourceLocationInstruction(inStateDefinition.stateToken().selection().start())
            );

        context.currentState(state);
        StateActionCompiler.compile(context, inStateDefinition.actionBlock());
        context.currentState(null);

        programBuilder.instruction(new JumpToInstruction(state));
    }

    private static void compileVariableDefinition(CompilationContext context, VariableDefinition definition) {
        var variableName = definition.variableName();

        checkDuplicateIdentifier(context, variableName, definition.variableNameToken().selection());

        var programBuilder = context.programBuilder();

        programBuilder.instruction(
            new SourceLocationInstruction(definition.initialVariableValue().selection().start())
        );

        var expressionType = ValueExpressionCompiler.compile(context, definition.initialVariableValue());

        context.variables().put(variableName, expressionType);

        programBuilder.instruction(new StoreInstruction(variableName));
    }

    private static void compileConstantDefinition(CompilationContext context, ConstantDefinition definition) {
        var constantName = definition.constantName();

        checkDuplicateIdentifier(context, constantName, definition.constantNameToken().selection());

        var programBuilder = context.programBuilder();

        programBuilder.instruction(
            new SourceLocationInstruction(definition.initialConstantValue().selection().start())
        );

        var expressionType = ValueExpressionCompiler.compile(context, definition.initialConstantValue());

        context.constants().put(constantName, expressionType);

        programBuilder.instruction(new StoreInstruction(constantName));
    }

    private static void checkDuplicateIdentifier(
        CompilationContext context,
        String identifier,
        SourceSelection identifierSelection)
    {
        var identifiers = Stream.concat(
            Stream.concat(
                context.variables().keySet().stream(),
                context.constants().keySet().stream()
            ),
            context.stateMachineBuilder().definedStates().stream()
        );

        if (identifiers.anyMatch(identifier::equals)) {
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.DUPLICATE_IDENTIFIER)
                    .selection(identifierSelection)
            );
        }
    }
}
