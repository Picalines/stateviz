package com.statelang.compilation;

import java.util.HashMap;
import java.util.Map;

import com.statelang.ast.*;
import com.statelang.compilation.instruction.BinaryOperatorInstruction;
import com.statelang.compilation.instruction.LoadInstruction;
import com.statelang.compilation.instruction.PushInstruction;
import com.statelang.compilation.instruction.UnaryOperatorInstruction;
import com.statelang.compilation.symbol.ConstantSymbol;
import com.statelang.compilation.symbol.VariableSymbol;
import com.statelang.diagnostics.Report;
import com.statelang.model.*;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ValueExpressionCompiler {

    public static InstanceType<?> compile(CompilationContext context, ValueExpressionNode expressionNode) {
        if (expressionNode instanceof InvalidValueNode) {
            return UnknownInstanceType.INSTANCE;
        }

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
        if (leftType == UnknownInstanceType.INSTANCE || rightType == UnknownInstanceType.INSTANCE) {
            return leftType;
        }

        var instanceOperator = leftType.getOperator(binaryExpression.operator(), rightType).orElse(null);
        var expressionSelection = binaryExpression.selection();

        if (instanceOperator == null) {
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.UNDEFINED_OPERATOR)
                    .selection(expressionSelection)
                    .info(binaryExpression.operator().format(leftType.name(), rightType.name()))
            );

            return UnknownInstanceType.INSTANCE;
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
        if (rightType == UnknownInstanceType.INSTANCE) {
            return rightType;
        }

        var instanceOperator = rightType.getOperator(unaryExpression.operator()).orElse(null);
        var expressionSelection = unaryExpression.selection();

        if (instanceOperator == null) {
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.UNDEFINED_OPERATOR)
                    .selection(expressionSelection)
                    .info(unaryExpression.operator().format(rightType.name()))
            );

            return UnknownInstanceType.INSTANCE;
        }

        context.programBuilder()
            .instruction(UnaryOperatorInstruction.of(unaryExpression.operator()));

        return instanceOperator.returnType();
    }

    private static InstanceType<?> compileVariableNode(
        CompilationContext context,
        VariableValueNode variable)
    {
        var symbols = context.programBuilder().definedSymbols();
        var variableName = variable.identifier();

        InstanceType<?> variableType = UnknownInstanceType.INSTANCE;

        if (symbols.containsKey(variableName)) {
            var symbol = symbols.get(variableName);
            if (symbol instanceof VariableSymbol variableSymbol) {
                variableType = variableSymbol.variableType();
            } else if (symbol instanceof ConstantSymbol constantSymbol) {
                variableType = constantSymbol.constantType();
            } else {
                context.reporter().report(
                    Report.builder()
                        .kind(Report.Kind.VARIABLE_OR_CONSTANT_EXPECTED)
                        .selection(variable.selection())
                );
            }
        } else {
            context.reporter().report(
                Report.builder()
                    .kind(Report.Kind.UNDEFINED_VARIABLE)
                    .selection(variable.selection())
            );
        }

        context.programBuilder()
            .instruction(new LoadInstruction(variableName));

        return variableType;
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
