package com.statelang.parsing.lib;

import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
final class ManyWithDelimiterParser<T> extends Parser<List<T>> {

    private final Parser<T> elementParser;

    private final Parser<?> delimiterParser;

    @Override
    public ParserResult<List<T>> parse(ParserContext context) {
        var reader = context.reader();

        List<T> elements = new LinkedList<>();

        while (true) {
            var beforeElementLocation = reader.location();
            var elementResult = elementParser.parse(context);

            if (!elementResult.isSuccess()) {
                var error = elementResult.error();

                if (error.location().equals(beforeElementLocation) || reader.atEnd()) {
                    break;
                }

                return ParserResult.fromError(error);
            }

            elements.add(elementResult.value());

            var beforeDelimiterLocation = reader.location();
            var delimiterResult = delimiterParser.parse(context);

            if (!delimiterResult.isSuccess()) {
                var error = delimiterResult.error();

                if (error.location().equals(beforeDelimiterLocation) || reader.atEnd()) {
                    break;
                }

                return ParserResult.fromError(error);
            }
        }

        return ParserResult.fromValue(elements);
    }
}
