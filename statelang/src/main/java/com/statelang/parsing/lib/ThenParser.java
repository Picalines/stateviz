package com.statelang.parsing.lib;

import lombok.AllArgsConstructor;

@AllArgsConstructor
final class ThenParser<T, U> extends Parser<U> {

    private final Parser<T> currentParser;

    private final Parser<U> nextParser;

    @Override
    public ParserResult<U> parse(ParserContext context) {
        var result = currentParser.parse(context);

        return result.isSuccess()
                ? nextParser.parse(context)
                : ParserResult.fromError(result.error());
    }
}
