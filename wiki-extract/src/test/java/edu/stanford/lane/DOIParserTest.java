package edu.stanford.lane;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class DOIParserTest {

    DOIParser parser;
    
    @Before
    public void setUp() throws Exception {
        this.parser = new DOIParser();
    }
    
    @Test
    public final void testParse() {
        // basic
        assertEquals("10.1002/047084289x.rd071", this.parser.parse("10.1002/047084289x.rd071").get(0));
        assertEquals("10.1038/nmat3724", this.parser.parse("10.1038/nmat3724").get(0));
        // empty
        assertEquals(0, this.parser.parse("http://dx..doi.org/").size());
        // basic structure, won't resolve from DOI API, but still need to return what was sent so we can record it
        assertEquals("10.xyz/foo", this.parser.parse("10.xyz/foo").get(0));
        // odd base hostname
        assertEquals("10.1016/j.biocon.2012.09.011",
                this.parser.parse("http://dx..doi.org/10.1016/j.biocon.2012.09.011").get(0));
        // encoding
        assertEquals("10.1001/archderm.1965.01600170101020",
                this.parser.parse("//dx.doi.org/10.1001%2Farchderm.1965.01600170101020").get(0));
        assertEquals("10.1002/(sici)(1997)5:2<86::aid-nt6>3.0.co;2-7",
                this.parser.parse("//dx.doi.org/10.1002%2F(SICI)(1997)5:2%3C86::AID-NT6%3E3.0.CO;2-7").get(0));
        assertEquals("10.1002/pros.1131+[pii]", this.parser.parse("//dx.doi.org/10.1002%2Fpros.1131+%5Bpii%5D").get(0));
        assertEquals("10.1021/bi020640+", this.parser.parse("10.1021/bi020640+").get(0));
        assertEquals("10.1021/bi020640+", this.parser.parse("10.1021/bi020640%2B").get(0));
        // source DOI is double encoded ... won't resolve, but still need returned first-pass decoded DOI
        assertEquals("10.1002/(sici)1096-8644(199703)102:3\\%3c301::aid-ajpa1\\%3e3.0.co;2-y", this.parser
                .parse("//dx.doi.org/10.1002%2F(SICI)1096-8644(199703)102:3%5C%253C301::AID-AJPA1%5C%253E3.0.CO;2-Y").get(0));
        // shortened
        assertEquals("10.1177/0021943612436973", this.parser.parse("http://doi.org/3d2").get(0));
        assertEquals("10.5465/amj.2009.43669971", this.parser.parse("http://doi.org/bptps5").get(0));
        // bad DOIs
        assertEquals(0, this.parser.parse("http://doi.org/10").size());
        assertEquals(0, this.parser.parse("http://doi.org/012345678").size());
        assertEquals(0, this.parser.parse("http://www.doi.org/news/DOINewsApr11.html#1").size());
        // case normalization
        assertEquals("10.1016/s0007-0785(96)80056-1", this.parser.parse("http://doi.org/doi:10.1016/S0007-0785(96)80056-1").get(0));
        assertEquals("10.1016/s0007-0785(96)80056-1", this.parser.parse("http://doi.org/DOI:10.1016/S0007-0785(96)80056-1").get(0));
        assertEquals("10.1016/s0007-0785(96)80056-1", this.parser.parse("http://DOI.ORG/doi:10.1016/S0007-0785(96)80056-1").get(0));
        // space after doi: ... only 26 like this found on 2016-08-31
        assertEquals("10.1016/j.parkreldis.2010.02.011", this.parser.parse("http://dx.doi.org/doi: 10.1016/j.parkreldis.2010.02.011").get(0));
        // aliases
        assertEquals("10.1007/bf00140587", this.parser.parse("10.1007/bf00140587").get(0));
        assertEquals("10.1007/bf02423389", this.parser.parse("10.1007/bf00140587").get(1));
    }
}
