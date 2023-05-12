package com.statelang.parsing.lib;

import com.statelang.diagnostics.Reporter;
import com.statelang.tokenization.TokenReader;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class ParserContext {

    private final TokenReader reader;

    private final Reporter reporter;
}
