package com.statelang.parsing;

import com.statelang.tokenization.Token;
import com.statelang.tokenization.TokenKind;

public final class Parse {
    private Parse() {
    }

    public static Parser<Token> token(TokenKind tokenKind) {
        return TokenParser.of(tokenKind);
    }
}
