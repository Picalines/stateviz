package com.statelang.tokenization;

public final class TokenBookmark implements AutoCloseable {
    private final TokenReader reader;

    private final SourceLocation location;

    private final LinkedNodeList<Token>.Node tokenNode;

    private final LinkedNodeList<TokenBookmark>.Node bookmarkNode;

    private final LinkedNodeList<TokenBookmark> bookmarkList;

    private boolean discarded = false;

    TokenBookmark(
            TokenReader reader,
            SourceLocation location,
            LinkedNodeList<Token>.Node tokenNode,
            LinkedNodeList<TokenBookmark> bookmarkList) {
        this.reader = reader;
        this.location = location;
        this.tokenNode = tokenNode;

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

    TokenReader reader() {
        return reader;
    }

    LinkedNodeList<Token>.Node tokenNode() {
        return tokenNode;
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
