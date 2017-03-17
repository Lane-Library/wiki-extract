package edu.stanford.lane.extraction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class PubmedExtractorTest {

    
    @Test
    public final void testExtractor() {
    PubmedExtractor articleExtractor = new PubmedExtractor(null);    
    assertEquals("21677750", articleExtractor.doiToPmid("10.1038/nature10163"));
    assertEquals("", articleExtractor.doiToPmid("test"));
    assertEquals("", articleExtractor.doiToPmid("10.3987/com-10-s(e)27 "));
    assertEquals("", articleExtractor.doiToPmid("10.1002/14356007.a18 313"));
    assertEquals("8741866", articleExtractor.doiToPmid("10.1002/(sici)1096-8628(19960122)61:3<216::aid-ajmg5>3.0.co;2-s"));
    assertTrue(articleExtractor.pmidToPubTypes("23613077").contains("Review"));
    assertFalse(articleExtractor.pmidToPubTypes("foo").contains("Review"));
    }
}
