package com.statelang.ast;

import com.statelang.tokenization.SourceSelection;

public abstract class ValueExpressionNode {

    public abstract SourceSelection selection();
}
