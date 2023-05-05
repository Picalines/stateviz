package com.statelang.compilation;

import java.util.HashMap;
import java.util.Map;

import com.statelang.ast.*;
import com.statelang.compilation.result.BinaryOperatorInstruction;
import com.statelang.compilation.result.LoadInstruction;
import com.statelang.compilation.result.PushInstruction;
import com.statelang.compilation.result.UnaryOperatorInstruction;
import com.statelang.diagnostics.Report;
import com.statelang.model.*;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ValueExpressionCompiler {

    public static InstanceType<?> compile(CompilationContext context, ValueExpressionNode expressionNode) {
        if (expressionNode instanceof LiteralValueNode literal) {
            return compileLiteralNode(context, literal);
        }

        if (expressionNode instanceof VariableValueNode variable) {
            return compileVariableNode(context, variable);
        }

        if (expressionNode instanceof UnaryValueExpressionNode unaryExpression) {
            return compileUnaryExpression(context, unaryExpression);
        }

        if (expressionNode instanceof BinaryValueExpressionNode binaryExpression) {
            return compileBinaryExpression(context, binaryExpression);
        }

        throw new UnsupportedOperationException(
            expressionNode.getClass().getName() + " expression node is not implemented"
        );
    }

    private static InstanceType<?> compileBinaryExpression(
        CompilationContext context,
        BinaryValueExpressionNode binaryExpression)
    {
        var leftType = compile(context, binaryExpression.left());
        var rightType = compile(context, binaryExpression.right());
        if (leftType == InvalidInstanceType.INSTANCE || rightType == InvalidInstanceType.INSTANCE) {
            return leftType;
        }

        var instanceOperator = leftType.getOperator(binaryExpression.operator(), rightType).orElse(null);
        var expressionSelection = binaryExpression.selection();

        if (instanceOperator == null) {
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.UNDEFINED_OPERATOR)
                    .selection(expressionSelection)
            );

            return InvalidInstanceType.INSTANCE;
        }

        context.programBuilder()
            .instruction(BinaryOperatorInstruction.of(binaryExpression.operator()));

        return instanceOperator.returnType();
    }

    private static InstanceType<?> compileUnaryExpression(
        CompilationContext context,
        UnaryValueExpressionNode unaryExpression)
    {
        var rightType = compile(context, unaryExpression.right());
        if (rightType == InvalidInstanceType.INSTANCE) {
            return rightType;
        }

        var instanceOperator = rightType.getOperator(unaryExpression.operator()).orElse(null);
        var expressionSelection = unaryExpression.selection();

        if (instanceOperator == null) {
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.UNDEFINED_OPERATOR)
                    .selection(expressionSelection)
            );

            return InvalidInstanceType.INSTANCE;
        }

        context.programBuilder()
            .instruction(UnaryOperatorInstruction.of(unaryExpression.operator()));

        return instanceOperator.returnType();
    }

    private static InstanceType<?> compileVariableNode(
        CompilationContext context,
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
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.UNDEFINED_VARIABLE)
                    .selection(variable.selection())
            );

            return InvalidInstanceType.INSTANCE;
        }

        context.programBuilder()
            .instruction(new LoadInstruction(variableName));

        return type;
    }

    private static final Map<Class<?>, InstanceType<?>> literalInstanceTypeMap = new HashMap<>() {
        {
            put(NumberLiteralValue.class, NumberInstanceType.INSTANCE);
            put(StringLiteralValue.class, StringInstanceType.INSTANCE);
            put(BooleanLiteralValue.class, BooleanInstanceType.INSTANCE);
        }
    };

    private static InstanceType<?> compileLiteralNode(CompilationContext context, LiteralValueNode literal) {
        var type = literalInstanceTypeMap.get(literal.getClass());

        if (type == null) {
            throw new UnsupportedOperationException(
                literal.getClass().getName() + " literal type is not implemented"
            );
        }

        context.programBuilder()
            .instruction(new PushInstruction(literal.value()));

        return type;
    }
}
