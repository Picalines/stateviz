package com.statelang.ast;

import com.statelang.tokenization.Token;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public final class ConstantDefinition extends Definition {

    @Getter
    private final Token constantNameToken;

    @Getter
    private final ValueExpressionNode initialConstantValue;

    public String constantName() {
        return constantNameToken.text();
    }
}
