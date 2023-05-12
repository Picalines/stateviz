package com.statelang.tokenization;

import lombok.Getter;

public final class TokenBookmark implements AutoCloseable {
    @Getter
    private final TokenReader reader;

    @Getter
    private final SourceLocation location;

    @Getter
    private final LinkedNodeList<Token>.Node tokenNode;

    @Getter
    private final boolean atEnd;

    private final LinkedNodeList<TokenBookmark>.Node bookmarkNode;

    private final LinkedNodeList<TokenBookmark> bookmarkList;

    private boolean discarded = false;

    TokenBookmark(
        TokenReader reader,
        LinkedNodeList<Token>.Node tokenNode,
        LinkedNodeList<TokenBookmark> bookmarkList) {
        this.reader = reader;
        this.location = reader.location();
        this.tokenNode = tokenNode;
        atEnd = reader.atEnd();

        this.bookmarkList = bookmarkList;

        var nextBookmark = bookmarkList.first();
        var bookmarkLocation = reader.location();

        while (nextBookmark != null && nextBookmark.value.location.isBefore(bookmarkLocation)) {
            nextBookmark = nextBookmark.next();
        }

        this.bookmarkNode = nextBookmark != null
            ? bookmarkList.addBefore(nextBookmark, this)
            : bookmarkList.addLast(this);
    }

    public void discard() {
        if (discarded) {
            return;
        }

        discarded = true;

        bookmarkList.remove(bookmarkNode);
    }

    @Override
    public void close() {
        discard();
    }
}
