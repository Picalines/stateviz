package com.statelang.ast;

import com.statelang.tokenization.SourceSelection;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class InvalidValueNode extends ValueExpressionNode {

    private final SourceSelection selection;

    @Override
    public SourceSelection selection() {
        return selection;
    }
}
