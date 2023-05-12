package com.statelang.ast;

import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public final class ConditionalAction extends StateAction {

    @Getter
    private final ValueExpressionNode condition;

    @Getter
    private final StateActionBlock trueBlock;

    @Getter
    @Nullable
    private final StateActionBlock falseBlock;
}
