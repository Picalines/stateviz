package com.statelang.parsing.lib;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.statelang.diagnostics.Report;
import com.statelang.diagnostics.Reporter;
import com.statelang.tokenization.SourceText;
import com.statelang.tokenization.TokenReader;

public abstract class Parser<T> {
    public abstract ParserResult<T> parse(ParserContext context);

    public final Optional<T> tryParse(SourceText sourceText, Reporter reporter) {
        var reader = TokenReader.startReading(sourceText, reporter);

        var result = parse(new ParserContext(reader, reporter));

        if (!reader.atEnd()) {
            reporter.report(
                Report.builder()
                    .selection(reader.selection())
                    .kind(Report.Kind.END_OF_INPUT_EXPECTED)
                    .unexpectedTokenKind(reader.currentToken().kind())
            );

            while (reader.tryAdvance()) {
                // yield errors on invalid tokens
            }
        }

        if (!result.isSuccess()) {
            reporter.report(result.error());
            return Optional.empty();
        }

        return Optional.of(result.value());
    }

    public final <U> Parser<U> then(Parser<U> nextParser) {
        return new ThenParser<>(this, nextParser);
    }

    public final <U> Parser<U> then(Function<ParserState<T>, Parser<U>> nextParserCreator) {
        return new ThenParserWithState<>(this, nextParserCreator);
    }

    public final Parser<T> or(Parser<T> parser) {
        return new OrParser<>(this, parser);
    }

    public final <U> Parser<U> map(Function<T, U> successMapper) {
        return new Parser<U>() {
            @Override
            public ParserResult<U> parse(ParserContext context) {
                var result = Parser.this.parse(context);
                return result.isSuccess()
                    ? ParserResult.fromValue(successMapper.apply(result.value()))
                    : ParserResult.fromError(result.error());
            }
        };
    }

    public final <U> Parser<U> map(Supplier<U> successSupplier) {
        return map(ignored -> successSupplier.get());
    }

    public final <U> Parser<U> cast(Class<U> clazz) {
        return map(clazz::cast);
    }

    public final <U> Parser<U> as(U successValue) {
        return map(() -> successValue);
    }

    public final Parser<List<T>> many() {
        return new ManyParser<>(this);
    }

    public final Parser<List<T>> manyUntil(Parser<?> endParser) {
        return new ManyUntilParser<>(this, endParser);
    }

    public final Parser<List<T>> manyWithDelimiter(Parser<?> delimiter) {
        return new ManyWithDelimiterParser<>(this, delimiter);
    }

    public final Parser<T> recover(Parser<T> recoveryParser, @Nullable Report.Kind overrideReportKind) {
        Preconditions.checkArgument(recoveryParser != null, "recoveryParser is null");

        return new Parser<T>() {
            @Override
            public ParserResult<T> parse(ParserContext context) {
                var result = Parser.this.parse(context);

                if (!result.isSuccess()) {
                    var error = result.error();

                    if (overrideReportKind != null) {
                        error = error.toBuilder().kind(overrideReportKind).build();
                    }

                    context.reporter().report(error);

                    return recoveryParser.parse(context);
                }

                return result;
            }
        };
    }

    public final Parser<T> recover(Parser<T> recoveryParser) {
        return recover(recoveryParser, null);
    }

    public final Parser<T> recover(T defaultValue, @Nullable Report.Kind overrideReportKind) {
        return recover(Parse.success(defaultValue), overrideReportKind);
    }

    public final Parser<T> recover(Supplier<T> defaultValueSupplier, @Nullable Report.Kind overrideReportKind) {
        return recover(Parse.success(defaultValueSupplier), overrideReportKind);
    }

    public final Parser<T> recover(Supplier<T> defaultValueSupplier) {
        return recover(defaultValueSupplier, null);
    }

    public final Parser<T> recover(T defaultValue) {
        return recover(defaultValue, null);
    }

    public final Parser<T> followedBy(Parser<?> nextParser) {
        return then(result -> nextParser.map(result::value));
    }

    public final Parser<T> between(Parser<?> leftSideParser, Parser<?> rightSideParser) {
        return leftSideParser.then(then(result -> rightSideParser.map(result::value)));
    }
}
