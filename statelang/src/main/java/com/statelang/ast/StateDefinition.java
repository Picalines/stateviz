package com.statelang.ast;

import java.util.List;

import com.statelang.tokenization.Token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Accessors(fluent = true)
public final class StateDefinition extends Definition {

    @Getter
    private final List<String> states;

    @Getter
    private final Token stateToken;
}
