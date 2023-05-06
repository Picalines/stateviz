package com.statelang.compilation.symbol;

public final class StateSymbol extends Symbol {

    public StateSymbol(String stateName) {
        super(stateName);
    }

    public String stateName() {
        return id();
    }
}
