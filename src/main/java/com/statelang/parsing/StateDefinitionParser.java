package com.statelang.parsing;

import com.statelang.ast.StateDefinition;
import com.statelang.parsing.lib.Parse;
import com.statelang.parsing.lib.Parser;
import com.statelang.tokenization.Token;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.statelang.tokenization.Token.Kind.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StateDefinitionParser {

    public static final Parser<StateDefinition> parser = Parse.token(KEYWORD_STATE)
        .then(
            Parse.token(IDENTIFIER)
                .map(Token::text)
                .manyWithDelimiter(Parse.token(COMMA))
                .map(StateDefinition::new)
                .between(Parse.token(OPEN_CURLY_BRACE), Parse.token(CLOSE_CURLY_BRACE))
        );
}
