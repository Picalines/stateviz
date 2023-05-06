package com.stateviz.models;

import java.util.List;
import java.util.Optional;

import com.statelang.compilation.CompiledProgram;
import com.statelang.diagnostics.Report;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class CompilationResult {

    @Getter
    private final List<Report> reports;

    @Getter
    private final Optional<CompiledProgram> program;
}
