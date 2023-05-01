package com.statelang.interpretation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class InterpreterTransitionException extends RuntimeException {

    @Getter
    private final String newState;
}
