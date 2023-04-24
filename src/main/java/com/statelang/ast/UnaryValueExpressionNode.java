package com.statelang.ast;

import com.statelang.model.UnaryOperator;
import com.statelang.tokenization.SourceSelection;
import com.statelang.tokenization.Token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Accessors(fluent = true)
public final class UnaryValueExpressionNode extends ValueExpressionNode {

    @Getter
    private final UnaryOperator operator;

    @Getter
    private final ValueExpressionNode right;

    @Getter
    private final Token operatorToken;

    @Override
    public SourceSelection selection() {
        return SourceSelection.hull(operatorToken.selection(), right.selection());
    }
}
