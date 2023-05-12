package com.statelang.ast;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public final class StateActionBlock extends StateAction {

    @Getter
    private final List<StateAction> actions;
}
