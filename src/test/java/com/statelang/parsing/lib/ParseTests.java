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

import lombok.AllArgsConstructor;

class ParseTests {

    @Nested
    class TryParseTests {

        @Test
        void emptySource() {
            var tokenParser = Parse.token(Token.Kind.KEYWORD_STATE);

            var result = assertParsesWithErrors("", tokenParser);

            assertTrue(result.isEmpty());
        }

        @Test
        void singleToken() {
            var tokenParser = Parse.token(Token.Kind.KEYWORD_STATE);

            var parsedToken = assertParsesWithoutErrors("state", tokenParser);

            assertEquals(Token.Kind.KEYWORD_STATE, parsedToken.kind());
        }

        @Test
        void tooManyTokens() {
            var tokenParser = Parse.token(Token.Kind.KEYWORD_STATE);

            var result = assertParsesWithErrors("state 123", tokenParser);

            assertTrue(result.isPresent());

            var parsedToken = result.get();
            assertEquals(Token.Kind.KEYWORD_STATE, parsedToken.kind());
        }
    }

    @Test
    void success() {
        var parser = Parse.success("success");

        var result = assertParsesWithoutErrors("", parser);

        assertEquals("success", result);
    }

    @Test
    void error() {
        var parser = Parse.error(Report.Kind.UNEXPECTED_TOKEN);

        var result = assertParsesWithErrors("", parser);

        assertTrue(result.isEmpty());
    }

    @Test
    void oneOf() {
        var tokenKinds = new Token.Kind[] { Token.Kind.KEYWORD_STATE, Token.Kind.KEYWORD_LET, Token.Kind.KEYWORD_CONST };
        var tokens = new String[] { "state", "let", "const" };

        @SuppressWarnings("unchecked")
        var tokenParsers = (Parser<Token>[]) Stream.of(tokenKinds).map(Parse::token).toArray(Parser[]::new);

        var parser = Parse.oneOf(tokenParsers);

        int i = 0;
        for (var kind : tokenKinds) {
            var token = assertParsesWithoutErrors(tokens[i++], parser);

            assertEquals(kind, token.kind());
        }

        var errorResult = assertParsesWithErrors("123", parser);
        assertTrue(errorResult.isEmpty());
    }

    @Test
    void recursive() {
        var parser = Parse.<String>recursive(recursion -> {
            return Parse.token(Token.Kind.LITERAL_NUMBER).then(recursion)
                    .or(Parse.token(Token.Kind.DOT).map(() -> "success"));
        });

        var result = assertParsesWithoutErrors("1 2 3 .", parser);
        assertEquals("success", result);
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

        var termParser = Parse.token(Token.Kind.LITERAL_NUMBER)
                .map(token -> (Term) new TokenTerm(token));

        var operatorParser = Parse.token(Token.Kind.OPERATOR_PLUS);

        var parser = Parse.chain(termParser, operatorParser, (op, a, b) -> new SumTerm(a, b));

        var result = assertParsesWithoutErrors("1 + 2 + 3", parser);

        assertInstanceOf(SumTerm.class, result);
    }

    @Test
    void optional() {
        var parser = Parse.optional(Parse.token(Token.Kind.OPERATOR_MINUS).as(-1)).or(Parse.success(1))
                .then(sign -> Parse.token(Token.Kind.LITERAL_NUMBER)
                        .map(token -> Double.valueOf(token.text()))
                        .map(num -> sign.value() * num));

        assertEquals(-1.25, assertParsesWithoutErrors("-1.25", parser));
        assertEquals(1.25, assertParsesWithoutErrors("1.25", parser));
    }

    @Test
    void skipUntil() {
        var parser = Parse.skipUntil(Parse.token(Token.Kind.SEMICOLON))
                .map(() -> "success");

        assertEquals("success", assertParsesWithoutErrors("1 2 3 4 5 ;", parser));
        assertParsesWithErrors("1 2 3 4 5", parser);
    }
}
