package com.statelang.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.statelang.diagnostics.Reporter;
import com.statelang.tokenization.SourceText;
import com.statelang.tokenization.TokenKind;

class ParseTests {

    @Nested
    class TryParseTests {

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

    @Nested
    class CombinatorTests {

        @Test
        void then() {
            var parser = Parse.token(TokenKind.KEYWORD_LET)
                    .then(Parse.token(TokenKind.IDENTIFIER))
                    .then(Parse.token(TokenKind.OPERATOR_EQUALS))
                    .then(Parse.token(TokenKind.LITERAL_NUMBER));

            var sourceText = SourceText.fromString("test", "let x = 123");
            var reporter = new Reporter();

            var result = parser.tryParse(sourceText, reporter);

            assertTrue(result.isPresent());
            assertFalse(reporter.hasErrors());
        }

        @Test
        void thenWithState() {
            var parser = Parse.token(TokenKind.KEYWORD_LET)
                    .then(firstToken -> Parse.token(TokenKind.IDENTIFIER)
                            .then(Parse.success(() -> firstToken.value())));

            var sourceText = SourceText.fromString("test", "let x");
            var reporter = new Reporter();

            var result = parser.tryParse(sourceText, reporter);

            assertTrue(result.isPresent());
            assertFalse(reporter.hasErrors());

            assertEquals(result.get().kind(), TokenKind.KEYWORD_LET);
        }
    }
}
