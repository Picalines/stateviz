package com.statelang.ast;

import com.statelang.tokenization.Token;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public final class NumberLiteralValue extends LiteralValueNode {

    @Getter
    private final double value;

    public NumberLiteralValue(Token token, double value) {
        super(token);

        this.value = value;
    }
}
