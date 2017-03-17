package edu.stanford.lane.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class IncrementingHashMapTest {

    IncrementingHashMap map;

    @Before
    public void setUp() throws Exception {
        this.map = new IncrementingHashMap();
    }

    @Test
    public final void testIncrementer() {
        assertEquals(null, this.map.get("foo"));
        this.map.add("foo");
        assertTrue(this.map.containsKey("foo"));
        assertEquals(1, this.map.get("foo").intValue());
        this.map.add("foo");
        assertEquals(2, this.map.get("foo").intValue());
        this.map.add("foo");
        assertEquals(3, this.map.get("foo").intValue());
        this.map.add("bar");
        assertEquals(1, this.map.get("bar").intValue());
    }
}
