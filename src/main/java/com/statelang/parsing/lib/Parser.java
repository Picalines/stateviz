package com.statelang.parsing.lib;

import java.util.Optional;
import java.util.function.Function;

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
            reporter.report(Report.builder()
                    .selection(reader.selection())
                    .kind(Report.Kind.END_OF_INPUT_EXPECTED)
                    .unexpectedTokenKind(reader.currentToken().kind()));

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
}
