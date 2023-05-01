package com.statelang.ast;

import com.statelang.tokenization.Token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Accessors(fluent = true)
public final class InStateDefinition extends Definition {

    @Getter
    private final Token stateToken;

    @Getter
    private StateActionBlock actionBlock;

    public String state() {
        return stateToken.text();
    }
}
