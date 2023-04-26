package com.statelang.parsing.lib;

import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
final class ManyParser<T> extends Parser<List<T>> {

    private final Parser<T> elementParser;

    @Override
    public ParserResult<List<T>> parse(ParserContext context) {
        var reader = context.reader();

        List<T> elements = new LinkedList<>();

        while (!reader.atEnd()) {
            var beforeElementLocation = reader.location();
            var elementResult = elementParser.parse(context);

            if (!elementResult.isSuccess()) {
                var error = elementResult.error();

                if (error.location().equals(beforeElementLocation)) {
                    break;
                }

                return ParserResult.fromError(error);
            }

            elements.add(elementResult.value());
        }

        return ParserResult.fromValue(elements);
    }
}
