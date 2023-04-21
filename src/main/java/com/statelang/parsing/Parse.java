package com.statelang.parsing;

import java.util.function.Supplier;

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
        return ErrorParser.of(reportKind);
    }

    public static Parser<Token> token(TokenKind tokenKind) {
        return TokenParser.of(tokenKind);
    }
}
