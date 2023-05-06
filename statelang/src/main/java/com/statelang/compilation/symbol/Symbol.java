package com.statelang.compilation.symbol;

import lombok.Getter;

public abstract class Symbol {

    @Getter
    private final String id;

    public Symbol(String id) {
        this.id = id;
    }
}
