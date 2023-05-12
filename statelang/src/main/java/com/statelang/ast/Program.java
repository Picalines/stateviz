package com.statelang.ast;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public final class Program {

    @Getter
    private final List<Definition> definitions;
}
