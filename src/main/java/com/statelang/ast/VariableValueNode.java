package com.statelang.ast;

import com.statelang.tokenization.SourceSelection;
import com.statelang.tokenization.Token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Accessors(fluent = true)
public final class VariableValueNode extends ValueExpressionNode {

    @Getter
    private final Token token;

    public String identifier() {
        return token.text();
    }

    @Override
    public SourceSelection selection() {
        return token.selection();
    }
}
