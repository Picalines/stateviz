package com.statelang.ast;

import com.statelang.tokenization.SourceSelection;
import com.statelang.tokenization.Token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Accessors(fluent = true)
public abstract class LiteralValueNode extends ValueExpressionNode {

    @Getter
    private final Token token;

    @Override
    public final SourceSelection selection() {
        return token.selection();
    }
}
