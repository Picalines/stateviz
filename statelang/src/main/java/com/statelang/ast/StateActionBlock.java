package com.statelang.ast;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Accessors(fluent = true)
public final class StateActionBlock extends StateAction {

    @Getter
    private final List<StateAction> actions;
}
