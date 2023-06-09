package com.statelang.ast;

import com.statelang.model.BinaryOperator;
import com.statelang.tokenization.SourceSelection;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public final class BinaryValueExpressionNode extends ValueExpressionNode {

    @Getter
    private final BinaryOperator operator;

    @Getter
    private final ValueExpressionNode left;

    @Getter
    private final ValueExpressionNode right;

    @Override
    public SourceSelection selection() {
        return SourceSelection.hull(left.selection(), right.selection());
    }
}
