package edu.stanford.lane;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DOIParserTest {

    @Test
    public final void testParse() {
        assertEquals("", DOIParser.parse("http://dx..doi.org/"));
        assertEquals("10.xyz/foo", DOIParser.parse("10.xyz/foo"));
        assertEquals("10.1016/j.biocon.2012.09.011",
                DOIParser.parse("http://dx..doi.org/10.1016/j.biocon.2012.09.011"));
        assertEquals("10.1001/archderm.1965.01600170101020",
                DOIParser.parse("//dx.doi.org/10.1001%2Farchderm.1965.01600170101020"));
        assertEquals("10.1002/(SICI)(1997)5:2<86::AID-NT6>3.0.CO;2-7",
                DOIParser.parse("//dx.doi.org/10.1002%2F(SICI)(1997)5:2%3C86::AID-NT6%3E3.0.CO;2-7"));
        assertEquals("10.1002/(SICI)1096-8644(199703)102:3\\%3C301::AID-AJPA1\\%3E3.0.CO;2-Y", DOIParser
                .parse("//dx.doi.org/10.1002%2F(SICI)1096-8644(199703)102:3%5C%253C301::AID-AJPA1%5C%253E3.0.CO;2-Y"));
        assertEquals("10.1002/pros.1131 [pii]", DOIParser.parse("//dx.doi.org/10.1002%2Fpros.1131+%5Bpii%5D"));
        assertEquals("", DOIParser.parse("http://www.doi.org/news/DOINewsApr11.html#1"));
    }
}
