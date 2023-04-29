package com.statelang.model;

import java.util.function.Function;

public final class NumberInstanceType extends InstanceType<Double> {

    public static final NumberInstanceType INSTANCE = new NumberInstanceType();

    private NumberInstanceType() {
        super("number", Double.class, define -> {

            final var numberType = INSTANCE;
            final var booleanType = BooleanInstanceType.INSTANCE;

            define.operator(UnaryOperator.PLUS, numberType, Function.identity());
            define.operator(UnaryOperator.MINUS, numberType, number -> -number);

            define.operator(BinaryOperator.PLUS, numberType, numberType, (a, b) -> a + b);
            define.operator(BinaryOperator.MINUS, numberType, numberType, (a, b) -> a - b);
            define.operator(BinaryOperator.MULTIPLY, numberType, numberType, (a, b) -> a * b);
            define.operator(BinaryOperator.DIVIDE, numberType, numberType, (a, b) -> a / b); // TODO: zero division
            define.operator(BinaryOperator.MODULO, numberType, numberType, (a, b) -> a % b); // TODO: zero division

            define.operator(BinaryOperator.EQUALS, numberType, booleanType, (a, b) -> a.equals(b));
            define.operator(BinaryOperator.NOT_EQUALS, numberType, booleanType, (a, b) -> !a.equals(b));
            define.operator(BinaryOperator.LESS, numberType, booleanType, (a, b) -> a < b);
            define.operator(BinaryOperator.LESS_OR_EQUAL, numberType, booleanType, (a, b) -> a <= b);
            define.operator(BinaryOperator.GREATER, numberType, booleanType, (a, b) -> a > b);
            define.operator(BinaryOperator.GREATER_OR_EQUAL, numberType, booleanType, (a, b) -> a >= b);
        });
    }
}
