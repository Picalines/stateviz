package com.statelang.interpretation;

import java.util.Optional;

import com.statelang.tokenization.SourceLocation;

public sealed interface Instruction permits ActionInstruction, LabelInstruction {
    default Optional<SourceLocation> location() {
        return Optional.empty();
    }
}
