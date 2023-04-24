package com.statelang.parsing;

import com.statelang.ast.*;
import com.statelang.diagnostics.Report;
import com.statelang.model.BinaryOperator;
import com.statelang.model.UnaryOperator;
import com.statelang.parsing.lib.*;
import com.statelang.tokenization.Token;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.statelang.tokenization.Token.Kind.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValueExpressionParser {
    public static final Parser<NumberLiteralValue> numberLiteral = Parse.token(LITERAL_NUMBER).map(
        token -> new NumberLiteralValue(token, Double.parseDouble(token.text()))
    );

    public static final Parser<BooleanLiteralValue> booleanLiteral = Parse.token(LITERAL_BOOLEAN).map(
        token -> new BooleanLiteralValue(token, token.text().equals("true"))
    );

    public static final Parser<StringLiteralValue> stringLiteral = Parse.token(LITERAL_STRING).map(
        token -> new StringLiteralValue(
            token,
            token.text().substring(1, token.text().length() - 1)
        )
    );

    public static final Parser<VariableValueNode> variable = Parse.token(IDENTIFIER).map(VariableValueNode::new);

    public static final Parser<ValueExpressionNode> lambda = Parse.recursive(ValueExpressionParser::parseLambda);

    private static Parser<ValueExpressionNode> parseLambda(Parser<ValueExpressionNode> lambdaRecursion) {
        var innerExpression = lambdaRecursion.between(Parse.token(OPEN_PARENTHESIS), Parse.token(CLOSE_PARENTHESIS));

        var primaryTerm = Parse.oneOf(
            numberLiteral,
            booleanLiteral,
            stringLiteral,
            variable,
            innerExpression
        )
            .recover(
                Parse.success(InvalidValueNode.instance), Report.Kind.VALUE_EXPRESSION_EXPECTED
            );

        record TokenOperatorPair(Token token, UnaryOperator operator) {
        }

        var signedTerm = Parse.oneOf(
            Parse.token(OPERATOR_PLUS).map(token -> new TokenOperatorPair(token, UnaryOperator.PLUS)),
            Parse.token(OPERATOR_MINUS).map(token -> new TokenOperatorPair(token, UnaryOperator.MINUS))
        )
            .then(
                operator -> primaryTerm.map(
                    term -> new UnaryValueExpressionNode(
                        operator.value().operator(),
                        term,
                        operator.value().token()
                    )
                )
                    .cast(ValueExpressionNode.class)
            )
            .or(primaryTerm);

        var multiplication = Parse.chain(
            signedTerm,
            Parse.oneOf(
                Parse.token(OPERATOR_MULTIPLY).as(BinaryOperator.MULTIPLY),
                Parse.token(OPERATOR_MULTIPLY).as(BinaryOperator.DIVIDE),
                Parse.token(OPERATOR_MULTIPLY).as(BinaryOperator.MODULO)
            ),
            BinaryValueExpressionNode::new
        );

        var addition = Parse.chain(
            multiplication,
            Parse.oneOf(
                Parse.token(OPERATOR_PLUS).as(BinaryOperator.PLUS),
                Parse.token(OPERATOR_MINUS).as(BinaryOperator.MINUS)
            ),
            BinaryValueExpressionNode::new
        );

        var relation = addition.then(
            left -> Parse.oneOf(
                Parse.token(OPERATOR_EQUALS).as(BinaryOperator.EQUALS),
                Parse.token(OPERATOR_NOT_EQUALS).as(BinaryOperator.NOT_EQUALS),
                Parse.token(OPERATOR_LESS).as(BinaryOperator.LESS),
                Parse.token(OPERATOR_LESS_OR_EQUAL).as(BinaryOperator.LESS_OR_EQUAL),
                Parse.token(OPERATOR_GREATER).as(BinaryOperator.GREATER),
                Parse.token(OPERATOR_GREATER_OR_EQUAL).as(BinaryOperator.GREATER_OR_EQUAL)
            )
                .then(
                    operator -> addition.map(
                        right -> new BinaryValueExpressionNode(
                            operator.value(),
                            left.value(),
                            right
                        )
                    )
                        .cast(ValueExpressionNode.class)
                )
                .or(Parse.success(left::value))
        );

        var not = Parse.token(OPERATOR_NOT).then(
            notToken -> relation.map(
                right -> new UnaryValueExpressionNode(
                    UnaryOperator.NOT,
                    right,
                    notToken.value()
                )
            )
                .cast(ValueExpressionNode.class)
        )
            .or(relation);

        var and = Parse.chain(
            not,
            Parse.token(OPERATOR_AND).as(BinaryOperator.AND),
            BinaryValueExpressionNode::new
        );

        return Parse.chain(
            and,
            Parse.token(OPERATOR_OR).as(BinaryOperator.OR),
            BinaryValueExpressionNode::new
        );
    }
}
