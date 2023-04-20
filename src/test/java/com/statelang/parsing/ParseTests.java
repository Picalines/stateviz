package com.statelang.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.statelang.diagnostics.Reporter;
import com.statelang.tokenization.SourceText;
import com.statelang.tokenization.TokenKind;

class ParseTests {

    @Test
    void emptySource() {
        var tokenParser = Parse.token(TokenKind.KEYWORD_STATE);

        var sourceText = SourceText.fromString("test", "");
        var reporter = new Reporter();

        var result = tokenParser.tryParse(sourceText, reporter);

        assertTrue(result.isEmpty());
        assertTrue(reporter.hasErrors());
    }

    @Test
    void singleToken() {
        var tokenParser = Parse.token(TokenKind.KEYWORD_STATE);

        var sourceText = SourceText.fromString("test", "state");
        var reporter = new Reporter();

        var result = tokenParser.tryParse(sourceText, reporter);

        assertTrue(result.isPresent());
        assertEquals(reporter.reports().size(), 0);

        var parsedToken = result.get();
        assertEquals(parsedToken.kind(), TokenKind.KEYWORD_STATE);
    }

    @Test
    void tooManyTokens() {
        var tokenParser = Parse.token(TokenKind.KEYWORD_STATE);

        var sourceText = SourceText.fromString("test", "state 123");
        var reporter = new Reporter();

        var result = tokenParser.tryParse(sourceText, reporter);

        assertTrue(result.isPresent());
        assertTrue(reporter.hasErrors());

        var parsedToken = result.get();
        assertEquals(parsedToken.kind(), TokenKind.KEYWORD_STATE);
    }
}
