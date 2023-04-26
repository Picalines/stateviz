package com.statelang.parsing.lib;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import com.statelang.diagnostics.Report;
import com.statelang.tokenization.Token;

final class TokenParser extends Parser<Token> {

    private static final Hashtable<Token.Kind, TokenParser> instances = new Hashtable<>();

    private final Token.Kind tokenKind;

    private final List<Token.Kind> expectedTokenKinds;

    private TokenParser(Token.Kind tokenKind) {
        this.tokenKind = tokenKind;
        expectedTokenKinds = Arrays.asList(tokenKind);
    }

    public static TokenParser of(Token.Kind tokenKind) {
        return instances.computeIfAbsent(tokenKind, TokenParser::new);
    }

    @Override
    public ParserResult<Token> parse(ParserContext context) {
        var reader = context.reader();

        if (reader.atEnd()) {
            return ParserResult.fromError(
                Report.builder()
                    .selection(reader.selection())
                    .kind(Report.Kind.UNEXPECTED_END_OF_INPUT)
                    .expectedTokenKinds(expectedTokenKinds)
            );
        }

        var actualTokenKind = reader.currentToken().kind();

        if (actualTokenKind != tokenKind) {
            return ParserResult.fromError(
                Report.builder()
                    .selection(reader.selection())
                    .kind(Report.Kind.UNEXPECTED_TOKEN)
                    .expectedTokenKinds(expectedTokenKinds)
                    .unexpectedTokenKind(actualTokenKind)
            );
        }

        var result = ParserResult.fromValue(reader.currentToken());

        reader.tryAdvance();

        return result;
    }
}
