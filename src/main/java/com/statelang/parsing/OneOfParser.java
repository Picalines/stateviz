package com.statelang.parsing;

import com.google.common.base.Preconditions;
import com.statelang.diagnostics.Report;

final class OneOfParser<T> extends Parser<T> {

    private final Parser<T>[] parsers;

    public OneOfParser(Parser<T>[] parsers) {
        Preconditions.checkArgument(parsers.length >= 2, "OneOfParser expects at least two parsers");

        this.parsers = parsers.clone();
    }

    @Override
    public ParserResult<T> parse(ParserContext context) {
        var reader = context.reader();

        var startLocation = reader.location();
        Report bestError = null;

        for (var parser : parsers) {
            var result = parser.parse(context);

            if (result.isSuccess() || !result.error().location().equals(startLocation)) {
                return result;
            }

            bestError = bestError == null
                    ? result.error()
                    : Report.determineMostRelevant(bestError, result.error());
        }

        return ParserResult.fromError(bestError);
    }
}
