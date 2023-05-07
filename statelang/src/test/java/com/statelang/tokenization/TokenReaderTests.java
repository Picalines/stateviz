package com.statelang.tokenization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.statelang.diagnostics.Report;
import com.statelang.diagnostics.Reporter;

class TokenReaderTests {

    private TokenReader createReader(String text, Reporter reporter) {
        return TokenReader.startReading(SourceText.fromString("test", text), reporter);
    }

    @Test
    void emptySource() {
        var reporter = new Reporter();
        var reader = createReader("", reporter);

        assertTrue(reader.atEnd());
        assertFalse(reader.tryAdvance());

        assertFalse(reporter.hasErrors());
    }

    @Test
    void singleToken() {
        var reporter = new Reporter();
        var reader = createReader("token", reporter);

        assertFalse(reader.atEnd());

        var token = reader.currentToken();
        assertNotNull(token);
        assertEquals(Token.Kind.IDENTIFIER, token.kind());
        assertEquals(new SourceSelection(new SourceLocation(1, 1), new SourceLocation(1, 5)), token.selection());

        assertFalse(reader.tryAdvance());
        assertTrue(reader.atEnd());
        assertSame(token, reader.currentToken());

        assertFalse(reporter.hasErrors());
    }

    @Test
    void ignoredTokens() {
        var reporter = new Reporter();
        var reader = createReader("# comment\n token] ", reporter);

        assertFalse(reader.atEnd());

        var token = reader.currentToken();
        assertNotNull(token);
        assertEquals(Token.Kind.IDENTIFIER, token.kind());
        assertEquals(new SourceSelection(new SourceLocation(2, 2), new SourceLocation(2, 6)), token.selection());

        assertFalse(reader.tryAdvance());
        assertTrue(reader.atEnd());
        assertEquals(token, reader.currentToken());

        assertTrue(reporter.hasErrors());
        assertEquals(1, reporter.reports().size());

        var invalidCharReport = reporter.reports().get(0);
        assertEquals(Report.Severity.ERROR, invalidCharReport.severity());

        var invalidChatLocation = new SourceLocation(2, 7);
        assertEquals(invalidChatLocation.toCharSelection(), invalidCharReport.selection());
    }

    @Test
    void threeNumbers() {
        var reporter = new Reporter();
        var reader = createReader("0 1 2", reporter);

        for (int num = 0; num < 3; num++) {
            assertFalse(reader.atEnd());

            var currentToken = reader.currentToken();
            assertNotNull(currentToken);

            assertEquals(Token.Kind.LITERAL_NUMBER, currentToken.kind());
            assertEquals(Integer.toString(num), currentToken.text());

            reader.tryAdvance();
        }

        assertTrue(reader.atEnd());
        assertFalse(reader.tryAdvance());

        assertFalse(reporter.hasErrors());
    }

    @Test
    void threeNumbersBacktracked() {
        var reporter = new Reporter();
        var reader = createReader("0 1 2", reporter);

        var firstBookmark = reader.createBookmark();

        for (int i = 0; i < 2; i++) {
            for (int num = 0; num < 3; num++) {
                assertFalse(reader.atEnd());

                var currentToken = reader.currentToken();
                assertNotNull(currentToken);

                assertEquals(Token.Kind.LITERAL_NUMBER, currentToken.kind());
                assertEquals(Integer.toString(num), currentToken.text());

                reader.tryAdvance();
            }

            assertTrue(reader.atEnd());
            assertFalse(reader.tryAdvance());

            assertFalse(reporter.hasErrors());

            if (i == 0) {
                reader.backtrackTo(firstBookmark);
                firstBookmark.close();
            }
        }
    }

    @Test
    void keywordOverIdentifier() {
        var sourceBuilder = new StringBuilder();

        var addedAnyKeyword = false;
        for (var kind : Token.Kind.values()) {
            if (!kind.name().startsWith("KEYWORD")) {
                continue;
            }

            sourceBuilder.append(kind.regex().pattern().replace("\\b", ""));
            sourceBuilder.append('\n');

            addedAnyKeyword = true;
        }

        assertTrue(addedAnyKeyword);

        var sourceText = SourceText.fromString("test", sourceBuilder.toString());

        var reporter = new Reporter();
        var reader = TokenReader.startReading(sourceText, reporter);

        while (!reader.atEnd()) {
            var token = reader.currentToken();
            assertNotNull(token);
            assertNotEquals(token.kind(), Token.Kind.IDENTIFIER);

            reader.tryAdvance();
        }
    }

    @Test
    void booleanLiteralOverIdentifier() {
        var reader = createReader("true\nfalse", new Reporter());

        while (!reader.atEnd()) {
            var token = reader.currentToken();
            assertNotNull(token);
            assertNotEquals(token.kind(), Token.Kind.IDENTIFIER);

            reader.tryAdvance();
        }
    }

    @Test
    void stringLiteralOverKeyword() {
        var reader = createReader("\"state\"", new Reporter());

        assertFalse(reader.tryAdvance());
        assertNotNull(reader.currentToken());
        assertEquals(Token.Kind.LITERAL_STRING, reader.currentToken().kind());
    }
}
