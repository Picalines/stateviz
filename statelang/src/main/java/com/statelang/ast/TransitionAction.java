package com.statelang.ast;

import com.statelang.tokenization.Token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Accessors(fluent = true)
public final class TransitionAction extends StateAction {

    @Getter
    private final Token newStateToken;

    public String newState() {
        return newStateToken.text();
    }
}
