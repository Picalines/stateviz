package com.statelang.parsing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.statelang.tokenization.Token.Kind.*;

import java.util.Objects;

import com.statelang.ast.AssertionAction;
import com.statelang.ast.AssignmentAction;
import com.statelang.ast.ConditionalAction;
import com.statelang.ast.StateAction;
import com.statelang.ast.StateActionBlock;
import com.statelang.ast.TransitionAction;
import com.statelang.diagnostics.Report;
import com.statelang.parsing.lib.Parse;
import com.statelang.parsing.lib.Parser;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StateActionParser {

    public static final Parser<AssertionAction> assertion = Parse.token(KEYWORD_ASSERT)
        .then(ValueExpressionParser.lambda)
        .followedBy(Parse.token(SEMICOLON).recover(() -> null))
        .map(AssertionAction::new);

    public static final Parser<TransitionAction> transition = Parse.token(KEYWORD_STATE)
        .then(Parse.token(OPERATOR_ASSIGN))
        .then(Parse.token(IDENTIFIER))
        .followedBy(Parse.token(SEMICOLON).recover(() -> null))
        .map(TransitionAction::new);

    public static final Parser<AssignmentAction> assignment = Parse.token(IDENTIFIER)
        .then(
            varToken -> Parse.token(OPERATOR_ASSIGN)
                .then(ValueExpressionParser.lambda)
                .followedBy(Parse.token(SEMICOLON).recover(() -> null))
                .map(newValue -> new AssignmentAction(varToken.value(), newValue))
        );

    public static final Parser<ConditionalAction> conditional = Parse.token(KEYWORD_IF)
        .then(
            ValueExpressionParser.lambda.withError(Report.Kind.CONDITION_EXPECTED)
        )
        .then(
            condition -> Parse.ref(() -> StateActionParser.block).then(
                trueBlock -> Parse.optional(
                    Parse.token(KEYWORD_ELSE)
                        .then(Parse.ref(() -> StateActionParser.block).recover(() -> null))
                        .map(falseBlock -> new ConditionalAction(condition.value(), trueBlock.value(), falseBlock))
                )
                    .or(
                        Parse.success(() -> new ConditionalAction(condition.value(), trueBlock.value(), null))
                    )
            )
        );

    private static final Parser<StateAction> action = Parse.oneOf(
        assertion,
        transition,
        assignment,
        conditional
    );

    public static final Parser<StateActionBlock> block = Parse.token(OPEN_CURLY_BRACE)
        .then(
            action
                .recover(
                    Parse.skipUntil(
                        Parse.oneOf(Parse.token(CLOSE_CURLY_BRACE), Parse.token(SEMICOLON))
                    )
                        .as(null)
                )
                .manyUntil(Parse.token(CLOSE_CURLY_BRACE))
                .map(actions -> actions.stream().filter(Objects::nonNull).toList())
                .map(StateActionBlock::new)
        );
}
