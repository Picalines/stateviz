package com.statelang.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Accessors(fluent = true)
public class AssertionAction extends StateAction {

    @Getter
    private final ValueExpressionNode assertedCondition;
}
