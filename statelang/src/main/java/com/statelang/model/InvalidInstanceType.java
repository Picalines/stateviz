package com.statelang.model;

public final class InvalidInstanceType extends InstanceType<Object> {

    public static final InvalidInstanceType INSTANCE = new InvalidInstanceType();

    private InvalidInstanceType() {
        super("<invalid>", Object.class, define -> {});
    }
}
