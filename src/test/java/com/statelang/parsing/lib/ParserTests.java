package com.statelang.parsing.lib;

import static com.statelang.parsing.lib.ParsingTestUtils.assertParsesWithoutErrors;
import static com.statelang.parsing.lib.ParsingTestUtils.assertParsesWithErrors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

import com.statelang.tokenization.Token;

class ParserTests {

    @Test
    void then() {
        var parser = Parse.token(Token.Kind.KEYWORD_LET)
                .then(Parse.token(Token.Kind.IDENTIFIER))
                .then(Parse.token(Token.Kind.OPERATOR_EQUALS))
                .then(Parse.token(Token.Kind.LITERAL_NUMBER));

        assertParsesWithoutErrors("let x = 123", parser);
    }

    @Test
    void thenWithState() {
        var parser = Parse.token(Token.Kind.KEYWORD_LET)
                .then(firstToken -> Parse.token(Token.Kind.IDENTIFIER)
                        .then(Parse.success(firstToken::value)));

        var token = assertParsesWithoutErrors("let x", parser);

        assertEquals(Token.Kind.KEYWORD_LET, token.kind());
    }

    @Test
    void or() {
        var parser = Parse.token(Token.Kind.KEYWORD_CONST)
                .or(Parse.token(Token.Kind.KEYWORD_LET));

        var token = assertParsesWithoutErrors("const", parser);
        assertEquals(Token.Kind.KEYWORD_CONST, token.kind());

        token = assertParsesWithoutErrors("let", parser);
        assertEquals(Token.Kind.KEYWORD_LET, token.kind());
    }

    @Test
    void map() {
        var parser = Parse.token(Token.Kind.LITERAL_NUMBER)
                .map(token -> Double.valueOf(token.text()));

        var number = assertParsesWithoutErrors("123.5", parser);
        assertEquals(123.5d, number);
    }

    @Test
    void as() {
        var parser = Parse.token(Token.Kind.OPERATOR_PLUS).as(1)
                .or(Parse.token(Token.Kind.OPERATOR_MINUS).as(-1));

        assertEquals(1, assertParsesWithoutErrors("+", parser));
        assertEquals(-1, assertParsesWithoutErrors("-", parser));

        assertParsesWithErrors("", parser);
    }

    @Test
    void many() {
        var numberParser = Parse.token(Token.Kind.LITERAL_NUMBER)
                .map(token -> Double.valueOf(token.text()));

        var parser = numberParser.many();

        assertIterableEquals(Arrays.asList(1.0, 2.0, 3.0), assertParsesWithoutErrors("1 2 3", parser));
        assertIterableEquals(Arrays.asList(1.0, 2.0, 3.0), assertParsesWithErrors("1 2 3 x", parser).get());
    }

    @Test
    void recover() {
        var parser = Parse.token(Token.Kind.KEYWORD_LET)
                .then(Parse.token(Token.Kind.IDENTIFIER))
                .map(Token::text)
                .then(varName -> Parse.token(Token.Kind.SEMICOLON).recover((Token) null)
                        .map(varName::value));

        assertEquals("x", assertParsesWithoutErrors("let x;", parser));

        var result = assertParsesWithErrors("let y", parser);
        assertTrue(result.isPresent());
        assertEquals("y", result.get());
    }
}
