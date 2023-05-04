package com.statelang.compilation;

import com.statelang.ast.AssertionAction;
import com.statelang.ast.AssignmentAction;
import com.statelang.ast.ConditionalAction;
import com.statelang.ast.StateAction;
import com.statelang.ast.StateActionBlock;
import com.statelang.ast.TransitionAction;
import com.statelang.diagnostics.Report;
import com.statelang.interpretation.InterpreterExitReason;
import com.statelang.model.BooleanInstanceType;
import com.statelang.model.InvalidInstanceType;
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

        context.interpreterBuilder()
            .instruction(c -> c.performTransition(newState))
            .instruction(c -> c.jumpTo(newState));
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

        context.interpreterBuilder().instruction(c -> {
            var variableValue = c.stack().pop();
            c.namedValues().put(variableName, variableValue);
        });
    }

    private static void compileConditional(
        CompilationContext context,
        ConditionalAction conditionalAction)
    {
        var conditionType = ValueExpressionCompiler.compile(context, conditionalAction.condition());

        if (conditionType != InvalidInstanceType.INSTANCE && conditionType != BooleanInstanceType.INSTANCE) {
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.TYPE_ERROR)
                    .selection(conditionalAction.condition().selection())
            );
        }

        var interpreterBuilder = context.interpreterBuilder();

        var endLabel = interpreterBuilder.generateLabel("$if_end");
        var falseBranchLabel = interpreterBuilder.generateLabel("$if_false");

        interpreterBuilder.instruction(c -> {
            var conditionValue = c.stack().pop();
            if (conditionValue == Boolean.FALSE) {
                c.jumpTo(falseBranchLabel);
            }
        });

        compile(context, conditionalAction.trueBlock());

        interpreterBuilder
            .instruction(c -> c.jumpTo(endLabel))
            .jumpLabel(falseBranchLabel);

        var falseBlock = conditionalAction.falseBlock();
        if (falseBlock != null) {
            compile(context, falseBlock);
        }

        interpreterBuilder.jumpLabel(endLabel);
    }

    private static void compileAssertion(
        CompilationContext context,
        AssertionAction assertionAction)
    {
        var conditionType = ValueExpressionCompiler.compile(context, assertionAction.condition());

        if (conditionType != InvalidInstanceType.INSTANCE && conditionType != BooleanInstanceType.INSTANCE) {
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.TYPE_ERROR)
                    .selection(assertionAction.condition().selection())
            );
        }

        var conditionLocation = assertionAction.condition().selection().start();

        context.interpreterBuilder().instruction(conditionLocation, c -> {
            var conditionValue = c.stack().pop();

            if (conditionValue == Boolean.FALSE) {
                c.exit(InterpreterExitReason.ASSERTION_FAILED);
            }
        });
    }
}
