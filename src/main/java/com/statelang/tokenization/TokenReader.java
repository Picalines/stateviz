package com.statelang.tokenization;

import java.util.Optional;

import com.statelang.diagnostics.Reporter;

public final class TokenReader {

    private final Tokenizer tokenizer;

    private LinkedNodeList<Token> cachedTokens = new LinkedNodeList<>();

    private LinkedNodeList<Token>.Node currentToken = null;

    private LinkedNodeList<TokenBookmark> bookmarks = new LinkedNodeList<>();

    private boolean atEnd = false;

    private TokenReader(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public static TokenReader startReading(SourceText sourceText, Reporter reporter) {
        return new TokenReader(new Tokenizer(sourceText, reporter));
    }

    public boolean atEnd() {
        return atEnd;
    }

    public Token currentToken() {
        return currentToken != null
            ? currentToken.value
            : null;
    }

    public SourceSelection selection() {
        return currentToken != null
            ? currentToken.value.selection()
            : SourceSelection.FIRST_CHARACTER;
    }

    public SourceLocation location() {
        return selection().start();
    }

    public boolean tryAdvance() {
        if (atEnd) {
            return false;
        }

        if (currentToken != null && currentToken.next() != null) {
            currentToken = currentToken.next();
            return true;
        }

        if (tokenizer.hasNext()) {
            var nextToken = tokenizer.next();
            if (nextToken != null) {
                currentToken = cachedTokens.addLast(nextToken);
                clearUnreachableTokens();
                return true;
            }
        }

        atEnd = true;
        return false;
    }

    public void createBookmark() {
        @SuppressWarnings("all")
        var bookmark = new TokenBookmark(this, location(), currentToken, bookmarks);
    }

    public void backtrackTo(TokenBookmark bookmark) {
        if (bookmark.reader() != this) {
            throw new IllegalArgumentException();
        }

        currentToken = bookmark.tokenNode();
    }

    private void clearUnreachableTokens() {
        var tokenNode = cachedTokens.first();
        var firstMarkedToken = Optional.ofNullable(bookmarks.first())
            .map(mark -> mark.value.tokenNode())
            .orElse(null);

        while (tokenNode != null && tokenNode != currentToken && tokenNode != firstMarkedToken) {
            tokenNode = tokenNode.next();
            cachedTokens.removeFirst();
        }
    }

    public static void main(String[] args) {
        var sourceText = SourceText.fromString("input", "1 2 3");
        var reporter = new Reporter();
        var reader = startReading(sourceText, reporter);

        while (reader.tryAdvance()) {
            System.out.println(reader.currentToken());
        }
    }
}
