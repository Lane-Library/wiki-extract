package edu.stanford.lane;

public class WikiExtractException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public WikiExtractException(final String message) {
        super(message);
    }

    public WikiExtractException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public WikiExtractException(final Throwable cause) {
        super(cause);
    }
}
