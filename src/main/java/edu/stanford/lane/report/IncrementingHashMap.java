package edu.stanford.lane.report;

import java.util.HashMap;

public class IncrementingHashMap extends HashMap<Object, Integer> {

    private static final long serialVersionUID = 1L;

    public void add(final Object key) {
        int count = 1;
        if (super.containsKey(key)) {
            count = super.get(key).intValue() + 1;
        }
        super.put(key, Integer.valueOf(count));
    }

}
