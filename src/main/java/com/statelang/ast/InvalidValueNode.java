package com.statelang.ast;

import com.statelang.tokenization.SourceSelection;

public final class InvalidValueNode extends ValueExpressionNode {

    public static final InvalidValueNode instance = new InvalidValueNode();

    private InvalidValueNode() {
    }

    @Override
    public SourceSelection selection() {
        throw new IllegalStateException("InvalidValueNode.selection");
    }
}
