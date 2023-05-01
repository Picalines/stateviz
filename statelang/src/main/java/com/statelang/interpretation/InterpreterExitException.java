package com.statelang.interpretation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class InterpreterExitException extends RuntimeException {

    @Getter
    private final InterpreterExitReason reason;
}
