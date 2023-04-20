package com.statelang.parsing;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import com.statelang.diagnostics.Report;
import com.statelang.tokenization.Token;
import com.statelang.tokenization.TokenKind;

final class TokenParser extends Parser<Token> {

    private static final Hashtable<TokenKind, TokenParser> instances = new Hashtable<>();

    private final TokenKind tokenKind;

    private final List<TokenKind> expectedTokenKinds;

    private TokenParser(TokenKind tokenKind) {
        this.tokenKind = tokenKind;
        expectedTokenKinds = Arrays.asList(tokenKind);
    }

    public static TokenParser of(TokenKind tokenKind) {
        return instances.computeIfAbsent(tokenKind, TokenParser::new);
    }

    @Override
    public ParserResult<Token> parse(ParserContext context) {
        var reader = context.reader();

        if (reader.atEnd()) {
            return ParserResult.fromError(Report.builder()
                    .selection(reader.selection())
                    .kind(Report.Kind.UNEXPECTED_END_OF_INPUT)
                    .expectedTokenKinds(expectedTokenKinds));
        }

        if (reader.currentToken().kind() != tokenKind) {
            return ParserResult.fromError(Report.builder()
                    .selection(reader.selection())
                    .kind(Report.Kind.UNEXPECTED_TOKEN)
                    .expectedTokenKinds(expectedTokenKinds));
        }

        var result = ParserResult.fromValue(reader.currentToken());

        reader.tryAdvance();

        return result;
    }
}
