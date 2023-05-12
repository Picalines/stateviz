package com.statelang.parsing;

import static com.statelang.tokenization.Token.Kind.CLOSE_CURLY_BRACE;
import static com.statelang.tokenization.Token.Kind.SEMICOLON;

import java.util.Objects;

import com.statelang.ast.Definition;
import com.statelang.ast.Program;
import com.statelang.parsing.lib.Parse;
import com.statelang.parsing.lib.Parser;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProgramParser {

    private static Parser<Definition> definition = Parse.oneOf(
        DefinitionParser.state,
        DefinitionParser.constant,
        DefinitionParser.variable,
        DefinitionParser.inState
    );

    public static Parser<Program> program = definition
        .recover(
            Parse.skipUntil(Parse.token(SEMICOLON, CLOSE_CURLY_BRACE)).as(null)
        )
        .many()
        .map(defs -> defs.stream().filter(Objects::nonNull).toList())
        .map(Program::new);
}
