package com.statelang.tokenization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        assertEquals(token.kind(), Token.Kind.IDENTIFIER);
        assertEquals(token.selection(), new SourceSelection(new SourceLocation(1, 1), new SourceLocation(1, 5)));

        assertFalse(reader.tryAdvance());
        assertTrue(reader.atEnd());
        assertEquals(reader.currentToken(), token);

        assertFalse(reporter.hasErrors());
    }

    @Test
    void ignoredTokens() {
        var reporter = new Reporter();
        var reader = createReader("# comment\n token] ", reporter);

        assertFalse(reader.atEnd());

        var token = reader.currentToken();
        assertNotNull(token);
        assertEquals(token.kind(), Token.Kind.IDENTIFIER);
        assertEquals(token.selection(), new SourceSelection(new SourceLocation(2, 2), new SourceLocation(2, 6)));

        assertFalse(reader.tryAdvance());
        assertTrue(reader.atEnd());
        assertEquals(reader.currentToken(), token);

        assertTrue(reporter.hasErrors());
        assertEquals(reporter.reports().size(), 1);

        var invalidCharReport = reporter.reports().get(0);
        assertEquals(invalidCharReport.severity(), Report.Severity.ERROR);

        var invalidChatLocation = new SourceLocation(2, 7);
        assertEquals(invalidCharReport.selection(), invalidChatLocation.toCharSelection());
    }

    @Test
    void threeNumbers() {
        var reporter = new Reporter();
        var reader = createReader("0 1 2", reporter);

        for (int num = 0; num < 3; num++) {
            assertFalse(reader.atEnd());

            var currentToken = reader.currentToken();
            assertNotNull(currentToken);

            assertEquals(currentToken.kind(), Token.Kind.LITERAL_NUMBER);
            assertEquals(currentToken.text(), Integer.toString(num));

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

                assertEquals(currentToken.kind(), Token.Kind.LITERAL_NUMBER);
                assertEquals(currentToken.text(), Integer.toString(num));

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
        var sourceText = SourceText.fromString("test", "true\nfalse");

        var reporter = new Reporter();
        var reader = TokenReader.startReading(sourceText, reporter);

        while (!reader.atEnd()) {
            var token = reader.currentToken();
            assertNotNull(token);
            assertNotEquals(token.kind(), Token.Kind.IDENTIFIER);

            reader.tryAdvance();
        }
    }

}
