package com.statelang.ast;

import com.statelang.tokenization.Token;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public final class AssignmentAction extends StateAction {

    @Getter
    private final Token variableToken;

    @Getter
    private final ValueExpressionNode newVariableValue;

    public String variableName() {
        return variableToken.text();
    }
}
