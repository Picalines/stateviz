package com.statelang.ast;

import com.statelang.tokenization.SourceSelection;
import com.statelang.tokenization.Token;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public abstract class LiteralValueNode extends ValueExpressionNode {

    @Getter
    private final Token token;

    public abstract Object value();

    @Override
    public final SourceSelection selection() {
        return token.selection();
    }
}
