package com.statelang.ast;

import com.statelang.tokenization.Token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Accessors(fluent = true)
public final class VariableDefinition extends Definition {

    @Getter
    private final Token variableNameToken;

    @Getter
    private final ValueExpressionNode initialVariableValue;

    public String variableName() {
        return variableNameToken.text();
    }
}
