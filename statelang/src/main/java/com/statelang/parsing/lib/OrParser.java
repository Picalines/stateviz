package com.statelang.parsing.lib;

import com.statelang.diagnostics.Report;

import lombok.AllArgsConstructor;

@AllArgsConstructor
final class OrParser<T> extends Parser<T> {
    private final Parser<T> firstParser;

    private final Parser<T> seconParser;

    @Override
    public ParserResult<T> parse(ParserContext context) {
        var startLocation = context.reader().location();

        var resultA = firstParser.parse(context);

        if (resultA.isSuccess()) {
            return resultA;
        }

        if (!resultA.error().location().equals(startLocation)) {
            return resultA;
        }

        var resultB = seconParser.parse(context);

        if (!resultB.isSuccess()) {
            return ParserResult.fromError(Report.determineMostRelevant(resultA.error(), resultB.error()));
        }

        return resultB;
    }
}
