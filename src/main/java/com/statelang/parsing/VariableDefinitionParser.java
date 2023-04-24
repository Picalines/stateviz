package com.statelang.parsing;

import com.statelang.ast.VariableDefinition;
import com.statelang.parsing.lib.Parse;
import com.statelang.parsing.lib.Parser;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.statelang.tokenization.Token.Kind.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VariableDefinitionParser {

    public static final Parser<VariableDefinition> parser = Parse.token(KEYWORD_LET)
        .then(Parse.token(IDENTIFIER))
        .then(
            nameToken -> Parse.token(OPERATOR_ASSIGN)
                .then(ValueExpressionParser.lambda)
                .followedBy(Parse.token(SEMICOLON).recover(() -> null))
                .map(initialValue -> new VariableDefinition(nameToken.value(), initialValue))
        );
}
