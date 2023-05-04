package com.statelang.interpretation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class LabelInstruction implements Instruction {

    @Getter
    private final String label;
}
