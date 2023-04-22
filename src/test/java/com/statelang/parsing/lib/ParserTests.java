package com.statelang.parsing.lib;

import static com.statelang.parsing.lib.ParsingTestUtils.assertParsesWithoutErrors;
import static com.statelang.parsing.lib.ParsingTestUtils.assertParsesWithErrors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.statelang.tokenization.TokenKind;

class ParserTests {

    @Test
    void then() {
        var parser = Parse.token(TokenKind.KEYWORD_LET)
                .then(Parse.token(TokenKind.IDENTIFIER))
                .then(Parse.token(TokenKind.OPERATOR_EQUALS))
                .then(Parse.token(TokenKind.LITERAL_NUMBER));

        assertParsesWithoutErrors("let x = 123", parser);
    }

    @Test
    void thenWithState() {
        var parser = Parse.token(TokenKind.KEYWORD_LET)
                .then(firstToken -> Parse.token(TokenKind.IDENTIFIER)
                        .then(Parse.success(() -> firstToken.value())));

        var token = assertParsesWithoutErrors("let x", parser);

        assertEquals(token.kind(), TokenKind.KEYWORD_LET);
    }

    @Test
    void or() {
        var parser = Parse.token(TokenKind.KEYWORD_CONST)
                .or(Parse.token(TokenKind.KEYWORD_LET));

        var token = assertParsesWithoutErrors("const", parser);
        assertEquals(token.kind(), TokenKind.KEYWORD_CONST);

        token = assertParsesWithoutErrors("let", parser);
        assertEquals(token.kind(), TokenKind.KEYWORD_LET);
    }

    @Test
    void map() {
        var parser = Parse.token(TokenKind.LITERAL_NUMBER)
                .map(token -> Double.valueOf(token.text()));

        var number = assertParsesWithoutErrors("123.5", parser);
        assertEquals(123.5d, number);
    }

    @Test
    void as() {
        var parser = Parse.token(TokenKind.OPERATOR_PLUS).as(1)
                .or(Parse.token(TokenKind.OPERATOR_MINUS).as(-1));

        assertEquals(1, assertParsesWithoutErrors("+", parser));
        assertEquals(-1, assertParsesWithoutErrors("-", parser));

        assertParsesWithErrors("", parser);
    }
}
