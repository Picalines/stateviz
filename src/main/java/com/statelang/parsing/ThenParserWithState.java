package com.statelang.parsing;

import java.util.function.Function;

final class ThenParserWithState<T, U> extends Parser<U> {
    private final Parser<T> currentParser;

    private final Parser<U> nextParser;

    private final ParserState<T> state;

    public ThenParserWithState(Parser<T> currentParser, Function<ParserState<T>, Parser<U>> nextParserCreator) {
        this.currentParser = currentParser;
        state = new ParserState<>();
        this.nextParser = nextParserCreator.apply(state);
    }

    public ParserResult<U> parse(ParserContext context) {
        var result = currentParser.parse(context);

        if (!result.isSuccess()) {
            return ParserResult.fromError(result.error());
        }

        state.pushValue(result.value());

        var nextResult = nextParser.parse(context);

        state.popValue();

        return nextResult;
    }
}
