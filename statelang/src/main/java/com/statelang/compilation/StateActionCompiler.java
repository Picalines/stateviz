package com.statelang.compilation;

import com.statelang.ast.*;
import com.statelang.compilation.instruction.*;
import com.statelang.compilation.symbol.ConstantSymbol;
import com.statelang.compilation.symbol.VariableSymbol;
import com.statelang.diagnostics.Report;
import com.statelang.model.*;
import com.statelang.tokenization.SourceLocation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class StateActionCompiler {

    public static void compile(CompilationContext context, StateAction action) {
        if (action instanceof TransitionAction transitionAction) {
            compileTransition(context, transitionAction);
            return;
        }

        if (action instanceof AssignmentAction assignmentAction) {
            compileAssignment(context, assignmentAction);
            return;
        }

        if (action instanceof ConditionalAction conditionalAction) {
            compileConditional(context, conditionalAction);
            return;
        }

        if (action instanceof AssertionAction assertionAction) {
            compileAssertion(context, assertionAction);
            return;
        }

        if (action instanceof StateActionBlock actionBlock) {
            compileBlock(context, actionBlock);
            return;
        }

        throw new UnsupportedOperationException(action.getClass().getName() + " state action is not implemented");
    }

    private static void compileBlock(
        CompilationContext context,
        StateActionBlock actionBlock)
    {
        SourceLocation reachableCodeEnd = null;

        for (var action : actionBlock.actions()) {
            if (reachableCodeEnd != null) {
                context.reporter().report(
                    Report.builder()
                        .kind(Report.Kind.UNREACHABLE_CODE)
                        .selection(reachableCodeEnd.toCharSelection())
                );
            } else {
                compile(context, action);

                if (action instanceof TransitionAction transitionAction) {
                    reachableCodeEnd = transitionAction.newStateToken().selection().end().shifted(0, 1);
                }
            }
        }
    }

    private static void compileTransition(
        CompilationContext context,
        TransitionAction transitionAction)
    {
        var newState = transitionAction.newState();
        var stateMachineBuilder = context.stateMachineBuilder();

        if (!stateMachineBuilder.definedStates().contains(newState)) {
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.UNDEFINED_STATE)
                    .selection(transitionAction.newStateToken().selection())
            );
        }

        stateMachineBuilder.transition(context.currentState(), newState);

        context.programBuilder()
            .instruction(
                new SourceLocationInstruction(transitionAction.newStateToken().selection().start())
            )
            .instruction(new JumpToInstruction(newState))
            .instruction(ExitInstruction.SUCCESS);
    }

    private static void compileAssignment(
        CompilationContext context,
        AssignmentAction assignmentAction)
    {
        var variableName = assignmentAction.variableName();

        var programBuilder = context.programBuilder();
        var symbols = programBuilder.definedSymbols();

        InstanceType<?> expectedType = UnknownInstanceType.INSTANCE;

        if (symbols.containsKey(variableName)) {
            var symbol = symbols.get(variableName);
            if (symbol instanceof VariableSymbol variableSymbol) {
                expectedType = variableSymbol.variableType();
            } else {
                context.reporter().report(
                    Report.builder()
                        .kind(
                            symbol instanceof ConstantSymbol
                                ? Report.Kind.CONSTANT_ASSIGNMENT
                                : Report.Kind.VARIABLE_EXPECTED
                        )
                        .selection(assignmentAction.variableToken().selection())
                );
            }
        } else {
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.UNDEFINED_VARIABLE)
                    .selection(assignmentAction.variableToken().selection())
            );
        }

        programBuilder.instruction(
            new SourceLocationInstruction(assignmentAction.variableToken().selection().start())
        );

        var valueType = ValueExpressionCompiler.compile(context, assignmentAction.newVariableValue());
        if (valueType == UnknownInstanceType.INSTANCE) {
            return;
        }

        if (expectedType != UnknownInstanceType.INSTANCE && valueType != expectedType) {
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.TYPE_ERROR)
                    .selection(assignmentAction.newVariableValue().selection())
            );
        }

        programBuilder.instruction(new StoreInstruction(variableName));
    }

    private static void compileConditional(
        CompilationContext context,
        ConditionalAction conditionalAction)
    {
        var programBuilder = context.programBuilder();

        var conditionLocation = conditionalAction.condition().selection().start();

        programBuilder.instruction(new SourceLocationInstruction(conditionLocation));

        var conditionType = ValueExpressionCompiler.compile(context, conditionalAction.condition());

        if (conditionType != UnknownInstanceType.INSTANCE && conditionType != BooleanInstanceType.INSTANCE) {
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.TYPE_ERROR)
                    .selection(conditionalAction.condition().selection())
            );
        }

        var uniqueLabelKey = conditionLocation.line() + "_" + conditionLocation.column();
        var endLabel = "$if_end" + uniqueLabelKey;
        var falseBranchLabel = "$if_false" + uniqueLabelKey;

        programBuilder.instruction(new JumpToIfNotInstruction(falseBranchLabel));

        compile(context, conditionalAction.trueBlock());

        programBuilder
            .instruction(new JumpToInstruction(endLabel))
            .instruction(new LabelInstruction(falseBranchLabel));

        var falseBlock = conditionalAction.falseBlock();
        if (falseBlock != null) {
            compile(context, falseBlock);
        }

        programBuilder.instruction(new LabelInstruction(endLabel));
    }

    private static void compileAssertion(
        CompilationContext context,
        AssertionAction assertionAction)
    {
        var programBuilder = context.programBuilder();

        var conditionLocation = assertionAction.condition().selection().start();

        programBuilder.instruction(new SourceLocationInstruction(conditionLocation));

        var conditionType = ValueExpressionCompiler.compile(context, assertionAction.condition());

        if (conditionType != UnknownInstanceType.INSTANCE && conditionType != BooleanInstanceType.INSTANCE) {
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.TYPE_ERROR)
                    .selection(assertionAction.condition().selection())
            );
        }

        var labelUniqueKey = conditionLocation.line() + "_" + conditionLocation.column();
        var successLabel = "$assert_true" + labelUniqueKey;
        var failureLabel = "$assert_false" + labelUniqueKey;

        programBuilder
            .instruction(new JumpToIfNotInstruction(failureLabel))
            .instruction(new JumpToInstruction(successLabel))
            .instruction(new LabelInstruction(failureLabel))
            .instruction(ExitInstruction.FAILURE)
            .instruction(new LabelInstruction(successLabel));
    }
}
