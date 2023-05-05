package com.statelang.model;

public final class BooleanInstanceType extends InstanceType<Boolean> {

    public static final BooleanInstanceType INSTANCE = new BooleanInstanceType();

    private BooleanInstanceType() {
        super("boolean", Boolean.class, define -> {

            final var booleanType = INSTANCE;

            define.operator(UnaryOperator.NOT, booleanType);

            define.operator(BinaryOperator.AND, booleanType, booleanType);
            define.operator(BinaryOperator.OR, booleanType, booleanType);

            define.operator(BinaryOperator.EQUALS, booleanType, booleanType);
            define.operator(BinaryOperator.NOT_EQUALS, booleanType, booleanType);
        });
    }
}
