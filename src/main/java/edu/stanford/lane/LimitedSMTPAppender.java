package edu.stanford.lane;

import org.apache.log4j.net.SMTPAppender;

public class LimitedSMTPAppender extends SMTPAppender {

    // default is per hour
    private int cycleSeconds = 3600;

    private long lastCycle;

    private int lastVisited;

    // max at 10 mails ...
    private int limit = 10;

    public void setCycleSeconds(final int cycleSeconds) {
        this.cycleSeconds = cycleSeconds;
    }

    public void setLimit(final int limit) {
        this.limit = limit;
    }

    @Override
    protected boolean checkEntryConditions() {
        final long now = System.currentTimeMillis();
        final long thisCycle = now - (now % (1000L * this.cycleSeconds));
        if (this.lastCycle != thisCycle) {
            this.lastCycle = thisCycle;
            this.lastVisited = 0;
        }
        this.lastVisited++;
        return this.lastVisited <= this.limit && super.checkEntryConditions();
    }
}