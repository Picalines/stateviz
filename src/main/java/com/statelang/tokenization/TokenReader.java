package com.statelang.tokenization;

import java.util.Iterator;
import java.util.Optional;

import com.google.common.collect.Iterators;
import com.statelang.diagnostics.Reporter;

public final class TokenReader {

    private final Iterator<Token> tokenizer;

    private LinkedNodeList<Token> cachedTokens = new LinkedNodeList<>();

    private LinkedNodeList<Token>.Node currentToken = null;

    private LinkedNodeList<TokenBookmark> bookmarks = new LinkedNodeList<>();

    private boolean atEnd = false;

    private TokenReader(Tokenizer tokenizer) {
        this.tokenizer = Iterators.filter(tokenizer, token -> token != null && !token.kind().ignored());

        tryAdvance();
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
            currentToken = cachedTokens.addLast(nextToken);
            clearUnreachableTokens();
            return true;
        }

        atEnd = true;
        return false;
    }

    public TokenBookmark createBookmark() {
        if (currentToken == null) {
            throw new IllegalStateException("cannot create bookmark at end");
        }

        return new TokenBookmark(this, location(), currentToken, bookmarks);
    }

    public void backtrackTo(TokenBookmark bookmark) {
        if (bookmark.reader() != this) {
            throw new IllegalArgumentException();
        }

        currentToken = bookmark.tokenNode();
        atEnd = false;
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
}
