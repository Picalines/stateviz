package com.statelang.ast;

import com.statelang.model.BinaryOperator;
import com.statelang.tokenization.SourceSelection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Accessors(fluent = true)
public final class BinaryValueExpressionNode extends ValueExpressionNode {

    @Getter
    private final ValueExpressionNode left;

    @Getter
    private final ValueExpressionNode right;

    @Getter
    private final BinaryOperator operator;

    @Override
    public SourceSelection selection() {
        return SourceSelection.hull(left.selection(), right.selection());
    }
}
