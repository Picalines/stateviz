package com.statelang.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class AssertionAction extends StateAction {

    @Getter
    private final ValueExpressionNode condition;
}
