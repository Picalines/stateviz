package com.statelang.ast;

import com.statelang.tokenization.Token;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public final class StringLiteralValue extends LiteralValueNode {

    @Getter
    private final String value;

    public StringLiteralValue(Token token, String value) {
        super(token);

        this.value = value;
    }
}