package com.statelang.model;

public final class StringInstanceType extends InstanceType<String> {

    public static final StringInstanceType INSTANCE = new StringInstanceType();

    private StringInstanceType() {
        super("string", String.class, define -> {

            final var stringType = INSTANCE;
            final var booleanType = BooleanInstanceType.INSTANCE;

            define.operator(BinaryOperator.PLUS, stringType, stringType);

            define.operator(BinaryOperator.EQUALS, stringType, booleanType);
            define.operator(BinaryOperator.NOT_EQUALS, stringType, booleanType);
        });
    }
}
