package com.statelang.model;

public final class UnknownInstanceType extends InstanceType<Object> {

    public static final UnknownInstanceType INSTANCE = new UnknownInstanceType();

    private UnknownInstanceType() {
        super("unknown", Object.class, define -> {});
    }
}
