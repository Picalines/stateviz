package com.langtools.tokenization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SourceText {
	private final String sourceDescriptor;

	private final String text;

	private SourceText(String sourceDescriptor, String text) {
		this.sourceDescriptor = sourceDescriptor;
		this.text = text;
	}

	public static SourceText fromString(String descriptor, String text) {
		return new SourceText(descriptor, text);
	}

	public static SourceText fromFile(Path filePath) throws IOException {
		var text = Files.readString(filePath);
		return new SourceText(filePath.getFileName().toString(), text);
	}

	public String getSourceDescriptor() {
		return sourceDescriptor;
	}

	public String getText() {
		return text;
	}
}
