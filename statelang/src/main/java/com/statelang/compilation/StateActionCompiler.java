package com.statelang.compilation;

import com.statelang.ast.*;
import com.statelang.compilation.result.*;
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

        var isVariableDefined = context.variables().containsKey(variableName);

        boolean isConstant = false;

        if (!isVariableDefined) {
            if (isConstant = context.constants().containsKey(variableName)) {
                context.reporter().report(
                    Report.builder()
                        .kind(Report.Kind.CONSTANT_ASSIGNMENT)
                        .selection(assignmentAction.variableToken().selection())
                );
            } else {
                context.reporter().report(
                    Report.builder()
                        .kind(Report.Kind.UNDEFINED_VARIABLE)
                        .selection(assignmentAction.variableToken().selection())
                );
            }
        }

        var programBuilder = context.programBuilder();

        programBuilder.instruction(
            new SourceLocationInstruction(assignmentAction.variableToken().selection().start())
        );

        var valueType = ValueExpressionCompiler.compile(context, assignmentAction.newVariableValue());

        if (isConstant || valueType == InvalidInstanceType.INSTANCE) {
            return;
        }

        var expectedType = context.variables().get(variableName);

        if (valueType != expectedType) {
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

        programBuilder.instruction(
            new SourceLocationInstruction(conditionalAction.condition().selection().start())
        );

        var conditionType = ValueExpressionCompiler.compile(context, conditionalAction.condition());

        if (conditionType != InvalidInstanceType.INSTANCE && conditionType != BooleanInstanceType.INSTANCE) {
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.TYPE_ERROR)
                    .selection(conditionalAction.condition().selection())
            );
        }

        var endLabel = programBuilder.generateLabel("$if_end");
        var falseBranchLabel = programBuilder.generateLabel("$if_false");

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

        programBuilder.instruction(
            new SourceLocationInstruction(
                assertionAction.condition().selection().start()
            )
        );

        var conditionType = ValueExpressionCompiler.compile(context, assertionAction.condition());

        if (conditionType != InvalidInstanceType.INSTANCE && conditionType != BooleanInstanceType.INSTANCE) {
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.TYPE_ERROR)
                    .selection(assertionAction.condition().selection())
            );
        }

        var successLabel = programBuilder.generateLabel("$assert_true");
        var failureLabel = programBuilder.generateLabel("$assert_false");

        programBuilder
            .instruction(new JumpToIfNotInstruction(failureLabel))
            .instruction(new JumpToInstruction(successLabel))
            .instruction(new LabelInstruction(failureLabel))
            .instruction(ExitInstruction.FAILURE)
            .instruction(new LabelInstruction(successLabel));
    }
}
