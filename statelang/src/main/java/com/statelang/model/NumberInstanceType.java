package com.statelang.model;

public final class NumberInstanceType extends InstanceType<Double> {

    public static final NumberInstanceType INSTANCE = new NumberInstanceType();

    private NumberInstanceType() {
        super("number", Double.class, define -> {

            final var numberType = INSTANCE;
            final var booleanType = BooleanInstanceType.INSTANCE;

            define.operator(UnaryOperator.PLUS, numberType);
            define.operator(UnaryOperator.MINUS, numberType);

            define.operator(BinaryOperator.PLUS, numberType, numberType);
            define.operator(BinaryOperator.MINUS, numberType, numberType);
            define.operator(BinaryOperator.MULTIPLY, numberType, numberType);
            define.operator(BinaryOperator.DIVIDE, numberType, numberType);
            define.operator(BinaryOperator.MODULO, numberType, numberType);

            define.operator(BinaryOperator.EQUALS, numberType, booleanType);
            define.operator(BinaryOperator.NOT_EQUALS, numberType, booleanType);
            define.operator(BinaryOperator.LESS, numberType, booleanType);
            define.operator(BinaryOperator.LESS_OR_EQUAL, numberType, booleanType);
            define.operator(BinaryOperator.GREATER, numberType, booleanType);
            define.operator(BinaryOperator.GREATER_OR_EQUAL, numberType, booleanType);
        });
    }
}
