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
        assertEquals("10.1002/(sici)(1997)5:2<86::aid-nt6>3.0.co;2-7",
                DOIParser.parse("//dx.doi.org/10.1002%2F(SICI)(1997)5:2%3C86::AID-NT6%3E3.0.CO;2-7"));
        // source DOI is double encoded ... would never resolve
        assertEquals("10.1002/(sici)1096-8644(199703)102:3\\%3c301::aid-ajpa1\\%3e3.0.co;2-y", DOIParser
                .parse("//dx.doi.org/10.1002%2F(SICI)1096-8644(199703)102:3%5C%253C301::AID-AJPA1%5C%253E3.0.CO;2-Y"));
        assertEquals("10.1002/pros.1131 [pii]", DOIParser.parse("//dx.doi.org/10.1002%2Fpros.1131+%5Bpii%5D"));
        assertEquals("", DOIParser.parse("http://www.doi.org/news/DOINewsApr11.html#1"));
        assertEquals("10.1177/0021943612436973", DOIParser.parse("http://doi.org/3d2"));
        assertEquals("10.5465/amj.2009.43669971", DOIParser.parse("http://doi.org/bptps5"));
        assertEquals("", DOIParser.parse("http://doi.org/10"));
        assertEquals("", DOIParser.parse("http://doi.org/012345678"));
        assertEquals("10.1016/s0007-0785(96)80056-1", DOIParser.parse("http://doi.org/doi:10.1016/S0007-0785(96)80056-1"));
        assertEquals("10.1016/s0007-0785(96)80056-1", DOIParser.parse("http://doi.org/DOI:10.1016/S0007-0785(96)80056-1"));
        assertEquals("10.1016/s0007-0785(96)80056-1", DOIParser.parse("http://DOI.ORG/doi:10.1016/S0007-0785(96)80056-1"));
        assertEquals("10.1002/047084289x.rd071", DOIParser.parse("10.1002/047084289x.rd071"));
    }
}
