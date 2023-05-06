package com.statelang.compilation.symbol;

import com.statelang.model.InstanceType;

import lombok.Getter;

public final class VariableSymbol extends Symbol {

    @Getter
    private final InstanceType<?> variableType;

    public VariableSymbol(String variableName, InstanceType<?> variableType) {
        super(variableName);
        this.variableType = variableType;
    }

    public String variableName() {
        return id();
    }
}
