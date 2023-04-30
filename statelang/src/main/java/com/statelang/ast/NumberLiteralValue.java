package com.statelang.ast;

import com.statelang.tokenization.Token;

import lombok.experimental.Accessors;

@Accessors(fluent = true)
public final class NumberLiteralValue extends LiteralValueNode {

    private final double value;

    @Override
    public Double value() {
        return Double.valueOf(value);
    }

    public NumberLiteralValue(Token token, double value) {
        super(token);

        this.value = value;
    }
}
