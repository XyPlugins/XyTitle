package org.xyplugin.xytitle.data;

public final class OwnedTitle {

    private final String titleId;
    private long expiresAtMillis;

    public OwnedTitle(String titleId, long expiresAtMillis) {
        this.titleId = titleId;
        this.expiresAtMillis = expiresAtMillis;
    }

    public String titleId() {
        return titleId;
    }

    public long expiresAtMillis() {
        return expiresAtMillis;
    }

    public void expiresAtMillis(long expiresAtMillis) {
        this.expiresAtMillis = expiresAtMillis;
    }

    public boolean expired() {
        return expiresAtMillis > 0L && System.currentTimeMillis() >= expiresAtMillis;
    }
}
