package com.statelang.parsing.lib;

import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
final class ManyUntilParser<T> extends Parser<List<T>> {

    private final Parser<T> elementParser;

    private final Parser<?> endParser;

    @Override
    public ParserResult<List<T>> parse(ParserContext context) {
        var reader = context.reader();

        List<T> elements = new LinkedList<>();

        while (true) {
            var beforeElementLocation = reader.location();

            var endResult = endParser.parse(context);
            if (endResult.isSuccess()) {
                break;
            }

            if (!endResult.error().location().equals(beforeElementLocation)) {
                return ParserResult.fromError(endResult.error());
            }

            var elementResult = elementParser.parse(context);

            if (!elementResult.isSuccess()) {
                return ParserResult.fromError(elementResult.error());
            }

            elements.add(elementResult.value());
        }

        return ParserResult.fromValue(elements);
    }
}
