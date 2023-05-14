package com.statelang.ast;

import java.util.List;

import com.statelang.tokenization.Token;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class StateDefinition extends Definition {

    @RequiredArgsConstructor
    @Getter
    public static final class State {

        private final Token nameToken;

        private final List<Attribute> attributes;

        public String name() {
            return nameToken.text();
        }
    }

    private final List<State> states;

    private final Token stateToken;
}
