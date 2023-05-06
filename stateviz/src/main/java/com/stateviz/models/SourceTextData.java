package com.stateviz.models;

import com.statelang.tokenization.SourceText;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class SourceTextData {

    @Getter
    private String descriptor;

    @Getter
    private String text;

    public SourceText toSourceText() {
        return SourceText.fromString(descriptor, text);
    }
}
