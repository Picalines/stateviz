package com.statelang.parsing.lib;

import lombok.AllArgsConstructor;

@AllArgsConstructor
final class ChainOperatorParser<TTerm, TOperator> extends Parser<TTerm> {

    private final Parser<TTerm> firstTermParser;

    private final Parser<TTerm> restTermsParser;

    private final Parser<TOperator> operatorParser;

    private final ChainOperatorApplier<TOperator, TTerm> apply;

    @Override
    public ParserResult<TTerm> parse(ParserContext context) {
        var firstItemResult = firstTermParser.parse(context);

        if (!firstItemResult.isSuccess()) {
            return firstItemResult;
        }

        return parseRest(context, firstItemResult.value());
    }

    private ParserResult<TTerm> parseRest(ParserContext context, TTerm firstTerm) {
        var beforeOperatorLocation = context.reader().location();

        var operatorResult = operatorParser.parse(context);

        if (!operatorResult.isSuccess()) {
            return operatorResult.error().location().equals(beforeOperatorLocation)
                    ? ParserResult.fromValue(firstTerm)
                    : ParserResult.fromError(operatorResult.error());
        }

        var secondTermResult = restTermsParser.parse(context);

        if (!secondTermResult.isSuccess()) {
            return secondTermResult;
        }

        return parseRest(context, apply.apply(operatorResult.value(), firstTerm, secondTermResult.value()));
    }
}
