package com.statelang.model;

public final class BooleanInstanceType extends InstanceType<Boolean> {

    public static final BooleanInstanceType INSTANCE = new BooleanInstanceType();

    private BooleanInstanceType() {
        super("boolean", Boolean.class, define -> {

            final var booleanType = INSTANCE;

            define.operator(UnaryOperator.NOT, booleanType, bool -> !bool);

            define.operator(BinaryOperator.AND, booleanType, booleanType, (a, b) -> a && b);
            define.operator(BinaryOperator.OR, booleanType, booleanType, (a, b) -> a || b);

            define.operator(BinaryOperator.EQUALS, booleanType, booleanType, (a, b) -> a.equals(b));
            define.operator(BinaryOperator.NOT_EQUALS, booleanType, booleanType, (a, b) -> !a.equals(b));
        });
    }
}
