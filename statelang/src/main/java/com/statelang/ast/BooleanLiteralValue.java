package com.statelang.ast;

import com.statelang.tokenization.Token;

public final class BooleanLiteralValue extends LiteralValueNode {

    private final boolean value;

    @Override
    public Boolean value() {
        return Boolean.valueOf(value);
    }

    public BooleanLiteralValue(Token token, boolean value) {
        super(token);

        this.value = value;
    }
}