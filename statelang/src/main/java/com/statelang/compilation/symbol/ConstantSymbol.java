package com.statelang.compilation.symbol;

import com.statelang.model.InstanceType;

import lombok.Getter;

public final class ConstantSymbol extends Symbol {

    @Getter
    private final InstanceType<?> constantType;

    public ConstantSymbol(String constantName, InstanceType<?> constantType) {
        super(constantName);
        this.constantType = constantType;
    }

    public String constantName() {
        return id();
    }
}
