package com.statelang.ast;

import com.statelang.tokenization.Token;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public final class BooleanLiteralValue extends LiteralValueNode {

    @Getter
    private final boolean value;

    public BooleanLiteralValue(Token token, boolean value) {
        super(token);

        this.value = value;
    }
}