package com.statelang.compilation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.statelang.ast.*;
import com.statelang.diagnostics.Report;
import com.statelang.diagnostics.Reporter;
import com.statelang.model.*;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ValueExpressionCompiler {

    public static CompiledExpression compile(CompilationContext context, ValueExpressionNode expressionNode) {
        var reporter = context.reporter();

        if (expressionNode instanceof LiteralValueNode literal) {
            return compileLiteralNode(literal);
        }

        if (expressionNode instanceof VariableValueNode variable) {
            return compileVariableNode(context, reporter, variable);
        }

        if (expressionNode instanceof UnaryValueExpressionNode unaryExpression) {
            return compileUnaryExpression(context, reporter, unaryExpression);
        }

        if (expressionNode instanceof BinaryValueExpressionNode binaryExpression) {
            return compileBinaryExpression(context, reporter, binaryExpression);
        }

        throw new UnsupportedOperationException(
            expressionNode.getClass().getName() + " expression node is not implemented"
        );
    }

    private static CompiledExpression compileBinaryExpression(CompilationContext context, Reporter reporter,
        BinaryValueExpressionNode binaryExpression)
    {
        var left = compile(context, binaryExpression.left());
        var right = compile(context, binaryExpression.right());
        if (left.type() == InvalidInstanceType.INSTANCE || right.type() == InvalidInstanceType.INSTANCE) {
            return left;
        }

        var evalLeft = left.action();
        var evalRight = right.action();

        var instanceOperator = left.type().getOperator(binaryExpression.operator(), right.type());
        var expressionSelection = binaryExpression.selection();

        return instanceOperator
            .map(operator -> new CompiledExpression(right.type(), evalLeft.andThen(evalRight).andThen(c -> {
                var rightValue = c.stack().pop();
                var leftValue = c.stack().pop();

                @SuppressWarnings("unchecked")
                var applyOperator = (BiFunction<Object, Object, Object>) operator.apply();

                c.stack().add(applyOperator.apply(leftValue, rightValue));
            })))
            .orElseGet(() -> {
                reporter.report(
                    Report.builder()
                        .kind(Report.Kind.UNDEFINED_OPERATOR)
                        .selection(expressionSelection)
                );

                return CompiledExpression.INVALID;
            });
    }

    private static CompiledExpression compileUnaryExpression(CompilationContext context, Reporter reporter,
        UnaryValueExpressionNode unaryExpression)
    {
        var right = compile(context, unaryExpression.right());
        if (right.type() == InvalidInstanceType.INSTANCE) {
            return right;
        }

        var evalRight = right.action();
        var instanceOperator = right.type().getOperator(unaryExpression.operator());
        var operatorToken = unaryExpression.operatorToken();

        return instanceOperator
            .map(operator -> new CompiledExpression(right.type(), evalRight.andThen(c -> {
                var rightValue = c.stack().pop();

                @SuppressWarnings("unchecked")
                var applyOperator = (Function<Object, Object>) operator.apply();

                c.stack().add(applyOperator.apply(rightValue));
            })))
            .orElseGet(() -> {
                reporter.report(
                    Report.builder()
                        .kind(Report.Kind.UNDEFINED_OPERATOR)
                        .selection(operatorToken.selection())
                );

                return CompiledExpression.INVALID;
            });
    }

    private static CompiledExpression compileVariableNode(CompilationContext context, Reporter reporter,
        VariableValueNode variable)
    {
        var variableName = variable.identifier();

        InstanceType<Object> type;

        if (context.variables().containsKey(variableName)) {
            @SuppressWarnings("unchecked")
            var variableType = (InstanceType<Object>) context.variables().get(variableName);
            type = variableType;
        } else if (context.constants().containsKey(variableName)) {
            @SuppressWarnings("unchecked")
            var constantType = (InstanceType<Object>) context.constants().get(variableName);
            type = constantType;
        } else {
            reporter.report(
                Report.builder()
                    .kind(Report.Kind.UNDEFINED_VARIABLE)
                    .selection(variable.selection())
            );

            return CompiledExpression.INVALID;
        }

        return new CompiledExpression(type, c -> c.stack().add(c.namedValues().get(variableName)));
    }

    private static final Map<Class<?>, InstanceType<?>> literalInstanceTypeMap = new HashMap<>() {
        {
            put(NumberLiteralValue.class, NumberInstanceType.INSTANCE);
            put(StringLiteralValue.class, StringInstanceType.INSTANCE);
            put(BooleanLiteralValue.class, BooleanInstanceType.INSTANCE);
        }
    };

    private static CompiledExpression compileLiteralNode(LiteralValueNode literal) {
        var type = literalInstanceTypeMap.get(literal.getClass());

        if (type == null) {
            throw new UnsupportedOperationException(
                literal.getClass().getName() + " literal type is not implemented"
            );
        }

        var value = literal.value();
        return new CompiledExpression(type, c -> c.stack().push(value));
    }
}
