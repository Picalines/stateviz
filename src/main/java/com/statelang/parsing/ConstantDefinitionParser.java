package com.statelang.parsing;

import com.statelang.ast.ConstantDefinition;
import com.statelang.parsing.lib.Parse;
import com.statelang.parsing.lib.Parser;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.statelang.tokenization.Token.Kind.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConstantDefinitionParser {

    public static final Parser<ConstantDefinition> parser = Parse.token(KEYWORD_CONST)
        .then(Parse.token(IDENTIFIER))
        .then(
            nameToken -> Parse.token(OPERATOR_ASSIGN)
                .then(ValueExpressionParser.lambda)
                .followedBy(Parse.token(SEMICOLON).recover(() -> null))
                .map(initialValue -> new ConstantDefinition(nameToken.value(), initialValue))
        );
}
