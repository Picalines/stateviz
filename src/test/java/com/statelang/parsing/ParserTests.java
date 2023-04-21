package com.statelang.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.statelang.tokenization.TokenKind;

import static com.statelang.parsing.ParsingTestUtils.assertParsesWithoutErrors;

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
}
