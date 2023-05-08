package com.stateviz.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.statelang.compilation.instruction.*;
import com.statelang.compilation.symbol.*;
import com.statelang.diagnostics.Report;
import com.statelang.model.InstanceType;
import com.statelang.tokenization.Token;

@Configuration
public class ObjectMapperConfiguration {

    @Bean
    ObjectMapper objectMapper() {
        var mapper = new ObjectMapper();

        mapper.registerModule(new Jdk8Module());

        mapper.addMixIn(InstanceType.class, InstanceTypeMixin.class);
        mapper.addMixIn(Instruction.class, InstructionMixin.class);
        mapper.addMixIn(Symbol.class, SymbolMixin.class);
        mapper.addMixIn(Report.class, ReportMixin.class);
        mapper.addMixIn(Token.Kind.class, TokenKindMixin.class);
        mapper.addMixIn(StateSymbol.class, StateSymbolMixin.class);
        mapper.addMixIn(VariableSymbol.class, VariableSymbolMixin.class);
        mapper.addMixIn(ConstantSymbol.class, ConstantSymbolMixin.class);

        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

        return mapper;
    }

    private interface InstanceTypeMixin {
        @JsonValue
        String name();
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type", defaultImpl = Void.class)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = BinaryOperatorInstruction.class, name = "bin_op"),
        @JsonSubTypes.Type(value = UnaryOperatorInstruction.class, name = "un_op"),
        @JsonSubTypes.Type(value = ExitInstruction.class, name = "exit"),
        @JsonSubTypes.Type(value = JumpToInstruction.class, name = "jump"),
        @JsonSubTypes.Type(value = JumpToIfNotInstruction.class, name = "jump_ifn"),
        @JsonSubTypes.Type(value = LabelInstruction.class, name = "label"),
        @JsonSubTypes.Type(value = PushInstruction.class, name = "push"),
        @JsonSubTypes.Type(value = StoreInstruction.class, name = "store"),
        @JsonSubTypes.Type(value = LoadInstruction.class, name = "load"),
        @JsonSubTypes.Type(value = SourceLocationInstruction.class, name = "src"),
    })
    private interface InstructionMixin {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type", defaultImpl = Void.class)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = StateSymbol.class, name = "state"),
        @JsonSubTypes.Type(value = VariableSymbol.class, name = "variable"),
        @JsonSubTypes.Type(value = ConstantSymbol.class, name = "constant"),
    })
    private interface SymbolMixin {
    }

    private interface ReportMixin {
        @JsonProperty
        Report.Severity severity();
    }

    private interface TokenKindMixin {
        @JsonValue
        String description();
    }

    private interface StateSymbolMixin {
        @JsonProperty
        String stateName();
    }

    private interface VariableSymbolMixin {
        @JsonProperty
        String variableName();
    }

    private interface ConstantSymbolMixin {
        @JsonProperty
        String constantName();
    }
}
