package com.statelang.compilation;

import java.util.stream.Stream;

import com.statelang.ast.ConstantDefinition;
import com.statelang.ast.Definition;
import com.statelang.ast.InStateDefinition;
import com.statelang.ast.StateDefinition;
import com.statelang.ast.VariableDefinition;
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

        if (stateMachineBuilder.hasDefinedStates()) {
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.AMBIGUOUS_DEFINITION)
                    .selection(stateDefinition.stateToken().selection())
            );
        }

        states.forEach(stateMachineBuilder::state);

        stateMachineBuilder.initialState(states.get(0));
    }

    private static void compileInStateDefinition(CompilationContext context, InStateDefinition inStateDefinition) {
        // TODO
    }

    private static void compileVariableDefinition(CompilationContext context, VariableDefinition definition) {
        checkDuplicateIdentifier(context, definition.variableName(), definition.variableNameToken().selection());

        var expression = ValueExpressionCompiler.compile(context, definition.initialVariableValue());
        var variableName = definition.variableName();

        context.variables().put(variableName, expression.type());

        context.interpreterBuilder().initAction(
            expression.action().andThen(c -> {
                var variableValue = c.stack().pop();
                c.namedValues().put(variableName, variableValue);
            })
        );
    }

    private static void compileConstantDefinition(CompilationContext context, ConstantDefinition definition) {
        checkDuplicateIdentifier(context, definition.constantName(), definition.constantNameToken().selection());

        var expression = ValueExpressionCompiler.compile(context, definition.initialConstantValue());
        var constantName = definition.constantName();

        context.constants().put(constantName, expression.type());

        context.interpreterBuilder().initAction(
            expression.action().andThen(c -> {
                var variableValue = c.stack().pop();
                c.namedValues().put(constantName, variableValue);
            })
        );
    }

    private static void checkDuplicateIdentifier(
        CompilationContext context,
        String identifier,
        SourceSelection identifierSelection)
    {
        var identifiers = Stream.concat(
            context.variables().keySet().stream(),
            context.constants().keySet().stream()
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
