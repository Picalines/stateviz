package com.statelang.parsing.lib;

import static com.statelang.parsing.lib.ParsingTestUtils.assertParsesWithErrors;
import static com.statelang.parsing.lib.ParsingTestUtils.assertParsesWithoutErrors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.statelang.diagnostics.Report;
import com.statelang.tokenization.Token;
import com.statelang.tokenization.TokenKind;

import lombok.AllArgsConstructor;

class ParseTests {

    @Nested
    class TryParseTests {

        @Test
        void emptySource() {
            var tokenParser = Parse.token(TokenKind.KEYWORD_STATE);

            var result = assertParsesWithErrors("", tokenParser);

            assertTrue(result.isEmpty());
        }

        @Test
        void singleToken() {
            var tokenParser = Parse.token(TokenKind.KEYWORD_STATE);

            var parsedToken = assertParsesWithoutErrors("state", tokenParser);

            assertEquals(parsedToken.kind(), TokenKind.KEYWORD_STATE);
        }

        @Test
        void tooManyTokens() {
            var tokenParser = Parse.token(TokenKind.KEYWORD_STATE);

            var result = assertParsesWithErrors("state 123", tokenParser);

            assertTrue(result.isPresent());

            var parsedToken = result.get();
            assertEquals(parsedToken.kind(), TokenKind.KEYWORD_STATE);
        }
    }

    @Test
    void success() {
        var parser = Parse.success("success");

        var result = assertParsesWithoutErrors("", parser);

        assertEquals(result, "success");
    }

    @Test
    void error() {
        var parser = Parse.error(Report.Kind.UNEXPECTED_TOKEN);

        var result = assertParsesWithErrors("", parser);

        assertTrue(result.isEmpty());
    }

    @Test
    void oneOf() {
        var tokenKinds = new TokenKind[] { TokenKind.KEYWORD_STATE, TokenKind.KEYWORD_LET, TokenKind.KEYWORD_CONST };
        var tokens = new String[] { "state", "let", "const" };

        @SuppressWarnings("unchecked")
        var tokenParsers = (Parser<Token>[]) Stream.of(tokenKinds).map(Parse::token).toArray(Parser[]::new);

        var parser = Parse.oneOf(tokenParsers);

        int i = 0;
        for (var kind : tokenKinds) {
            var token = assertParsesWithoutErrors(tokens[i++], parser);

            assertEquals(token.kind(), kind);
        }

        var errorResult = assertParsesWithErrors("123", parser);
        assertTrue(errorResult.isEmpty());
    }

    @Test
    void recursive() {
        var parser = Parse.<String>recursive(recursion -> {
            return Parse.token(TokenKind.LITERAL_NUMBER).then(recursion)
                    .or(Parse.token(TokenKind.DOT).then(Parse.success("success")));
        });

        var result = assertParsesWithoutErrors("1 2 3 .", parser);
        assertEquals(result, "success");
    }

    @Test
    void chain() {
        abstract class Term {
        }

        @AllArgsConstructor
        @SuppressWarnings("unused")
        final class TokenTerm extends Term {
            public final Token token;
        }

        @AllArgsConstructor
        @SuppressWarnings("unused")
        final class SumTerm extends Term {
            public final Term leftTerm;
            public final Term rightTerm;
        }

        var termParser = Parse.token(TokenKind.LITERAL_NUMBER)
                .then(token -> Parse.success(() -> (Term) new TokenTerm(token.value())));

        var operatorParser = Parse.token(TokenKind.OPERATOR_PLUS);

        var parser = Parse.chain(termParser, operatorParser, (op, a, b) -> new SumTerm(a, b));

        var result = assertParsesWithoutErrors("1 + 2 + 3", parser);

        assertInstanceOf(SumTerm.class, result);
    }
}
