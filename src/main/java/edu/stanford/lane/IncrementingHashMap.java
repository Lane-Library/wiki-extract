package edu.stanford.lane;

import java.util.HashMap;

public class IncrementingHashMap extends HashMap<Object, Integer> {

    private static final long serialVersionUID = 1L;

    private HashMap<Object, Integer> hashMap;

    public IncrementingHashMap() {
        this.hashMap = new HashMap<>();
    }

    public void add(final Object key) {
        int count = 1;
        if (this.hashMap.containsKey(key)) {
            count = this.hashMap.get(key).intValue() + 1;
        }
        this.hashMap.put(key, new Integer(count));
    }

    @Override
    public boolean containsKey(final Object key) {
        return this.hashMap.containsKey(key);
    }

    @Override
    public Integer get(final Object key) {
        return this.hashMap.get(key);
    }

    @Override
    public Integer put(final Object key, final Integer value) {
        return this.hashMap.put(key, value);
    }
}
