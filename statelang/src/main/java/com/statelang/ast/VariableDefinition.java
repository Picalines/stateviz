package com.statelang.ast;

import com.statelang.tokenization.Token;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public final class VariableDefinition extends Definition {

    @Getter
    private final Token variableNameToken;

    @Getter
    private final ValueExpressionNode initialVariableValue;

    public String variableName() {
        return variableNameToken.text();
    }
}
