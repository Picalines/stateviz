package com.statelang.ast;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Accessors(fluent = true)
public final class Program {

    @Getter
    private final List<Definition> definitions;
}
