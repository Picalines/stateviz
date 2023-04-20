package com.statelang.parsing;

import com.statelang.diagnostics.Reporter;
import com.statelang.tokenization.TokenReader;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Accessors(fluent = true)
public final class ParserContext {

    private final TokenReader reader;

    private final Reporter reporter;

}
