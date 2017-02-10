package edu.stanford.lane;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;

import org.junit.Before;
import org.junit.Test;

public class IncrementingHashMapTest {

    IncrementingHashMap map;

    @Before
    public void setUp() throws Exception {
        this.map = new IncrementingHashMap();
    }
    
    @Test
    public final void test2() {
        DateTimeFormatter dateFormat = DateTimeFormatter.ISO_INSTANT;
        TemporalAccessor accessor = dateFormat.parse("2016-05-21T14:59:09Z");
        System.out.println(accessor);
        TemporalField tfField;
        Instant i = Instant.parse("2016-05-21T14:59:09Z");
        System.out.println(i.isBefore(Instant.parse("2017-05-21T14:59:09Z")));
    }

    @Test
    public final void test() {
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
