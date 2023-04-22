package com.statelang.parsing.lib;

import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.statelang.diagnostics.Report;
import com.statelang.tokenization.Token;
import com.statelang.tokenization.TokenKind;

public final class Parse {
    private Parse() {
    }

    public static <T> Parser<T> success(Supplier<T> valueSupplier) {
        return new Parser<>() {
            @Override
            public ParserResult<T> parse(ParserContext context) {
                return ParserResult.fromValue(valueSupplier.get());
            }
        };
    }

    public static <T> Parser<T> success(T value) {
        return success(() -> value);
    }

    public static <T> Parser<T> error(Report.Kind reportKind) {
        Preconditions.checkArgument(reportKind != null, "reportKind is null");
        return ErrorParser.of(reportKind);
    }

    public static Parser<Token> token(TokenKind tokenKind) {
        Preconditions.checkArgument(tokenKind != null, "tokenKind is null");
        return TokenParser.of(tokenKind);
    }

    @SafeVarargs
    public static <T> Parser<T> oneOf(Parser<T>... parsers) {
        Preconditions.checkArgument(parsers.length >= 1, "Parse.oneOf expects at least one parser");

        return switch (parsers.length) {
            case 1 -> parsers[0];
            case 2 -> parsers[0].or(parsers[1]);
            default -> new OneOfParser<>(parsers);
        };
    }
}
