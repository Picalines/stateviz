package com.statelang.parsing;

import com.statelang.ast.ConstantDefinition;
import com.statelang.ast.InStateDefinition;
import com.statelang.ast.StateDefinition;
import com.statelang.ast.VariableDefinition;
import com.statelang.parsing.lib.Parse;
import com.statelang.parsing.lib.Parser;
import com.statelang.tokenization.Token;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.statelang.tokenization.Token.Kind.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefinitionParser {

    public static final Parser<ConstantDefinition> constant = Parse.token(KEYWORD_CONST)
        .then(Parse.token(IDENTIFIER))
        .then(
            nameToken -> Parse.token(OPERATOR_ASSIGN)
                .then(ValueExpressionParser.lambda)
                .followedBy(Parse.token(SEMICOLON).recover(() -> null))
                .map(initialValue -> new ConstantDefinition(nameToken.value(), initialValue))
        );

    public static final Parser<VariableDefinition> variable = Parse.token(KEYWORD_LET)
        .then(Parse.token(IDENTIFIER))
        .then(
            nameToken -> Parse.token(OPERATOR_ASSIGN)
                .then(ValueExpressionParser.lambda)
                .followedBy(Parse.token(SEMICOLON).recover(() -> null))
                .map(initialValue -> new VariableDefinition(nameToken.value(), initialValue))
        );

    public static final Parser<StateDefinition> state = Parse.token(KEYWORD_STATE)
        .then(
            stateKeyword -> Parse.token(IDENTIFIER)
                .map(Token::text)
                .manyWithDelimiter(Parse.token(COMMA))
                .map(states -> new StateDefinition(states, stateKeyword.value()))
                .between(Parse.token(OPEN_CURLY_BRACE), Parse.token(CLOSE_CURLY_BRACE))
        );

    public static final Parser<InStateDefinition> inState = Parse.token(KEYWORD_WHEN)
        .then(Parse.token(IDENTIFIER))
        .then(
            stateToken -> StateActionParser.block
                .map(actionBlock -> new InStateDefinition(stateToken.value(), actionBlock))
        );
}
