package com.statelang.interpretation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class InterpreterJumpException extends RuntimeException {

    @Getter
    private final String label;
}
