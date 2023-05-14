package com.statelang.ast;

import com.statelang.tokenization.Token;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Attribute {

    @Getter
    private final Token nameToken;

    @Getter
    private final Token valueToken;

    public String name() {
        return nameToken.text();
    }

    public String value() {
        var valueStr = valueToken.text();
        return valueStr.substring(1, valueStr.length() - 1);
    }
}
