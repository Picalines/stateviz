package com.statelang.parsing.lib;

import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.statelang.diagnostics.Report;
import com.statelang.tokenization.Token;

public final class Parse {
    private Parse() {
    }

    public static <T> Parser<T> success(Supplier<T> valueSupplier) {
        Preconditions.checkArgument(valueSupplier != null, "valueSupplier is null");
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

    public static Parser<Token> token(Token.Kind tokenKind) {
        Preconditions.checkArgument(tokenKind != null, "tokenKind is null");
        return TokenParser.of(tokenKind);
    }

    @SafeVarargs
    public static <T> Parser<T>[] commonBaseUpcast(Parser<? extends T>... parsers) {
        @SuppressWarnings("unchecked")
        var castedParsers = (Parser<T>[]) parsers;
        return castedParsers;
    }

    @SafeVarargs
    public static <T> Parser<T> oneOf(Parser<? extends T>... parsers) {
        Preconditions.checkArgument(parsers.length >= 1, "Parse.oneOf expects at least one parser");

        var castedParsers = commonBaseUpcast(parsers);

        return switch (parsers.length) {
            case 1 -> castedParsers[0];
            case 2 -> castedParsers[0].or(castedParsers[1]);
            default -> new OneOfParser<>(castedParsers);
        };
    }

    public static <T> Parser<T> recursive(Function<Parser<T>, Parser<T>> recursiveParserCreator) {
        Preconditions.checkArgument(recursiveParserCreator != null, "recursiveParserCreator is null");

        var recParser = new Parser<T>() {
            public Parser<T> recursiveRef = null;

            @Override
            public ParserResult<T> parse(ParserContext context) {
                return recursiveRef.parse(context);
            }
        };

        recParser.recursiveRef = recursiveParserCreator.apply(recParser);

        return recParser;
    }

    public static <TTerm, TOperator> Parser<TTerm> chain(
            Parser<TTerm> firstTermParser,
            Parser<TTerm> restTermsParser,
            Parser<TOperator> operatorParser,
            ChainOperatorApplier<TOperator, TTerm> apply) {
        Preconditions.checkArgument(firstTermParser != null, "firstTermParser is null");
        Preconditions.checkArgument(restTermsParser != null, "restTermsParser is null");
        Preconditions.checkArgument(operatorParser != null, "operatorParser is null");
        Preconditions.checkArgument(apply != null, "apply is null");

        return new ChainOperatorParser<>(firstTermParser, restTermsParser, operatorParser, apply);
    }

    public static <TTerm, TOperator> Parser<TTerm> chain(
            Parser<TTerm> termParser,
            Parser<TOperator> operatorParser,
            ChainOperatorApplier<TOperator, TTerm> apply) {
        return chain(termParser, termParser, operatorParser, apply);
    }

    public static <T> Parser<T> optional(Parser<T> parser) {
        Preconditions.checkArgument(parser != null, "parser is null");

        return new Parser<T>() {
            @Override
            public ParserResult<T> parse(ParserContext context) {
                var reader = context.reader();

                try (var startBookmark = reader.createBookmark()) {
                    var result = parser.parse(context);

                    if (!result.isSuccess()) {
                        reader.backtrackTo(startBookmark);
                    }

                    return result;
                }
            }
        };
    }

    public static <T> Parser<T> skipUntil(Parser<T> parser) {
        Preconditions.checkArgument(parser != null, "parser is null");

        return new Parser<T>() {
            @Override
            public ParserResult<T> parse(ParserContext context) {
                var reader = context.reader();

                do {
                    var result = parser.parse(context);

                    if (result.isSuccess()) {
                        return result;
                    }
                } while (reader.tryAdvance());

                return parser.parse(context);
            }
        };
    }
}
