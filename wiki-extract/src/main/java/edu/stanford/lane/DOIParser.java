package edu.stanford.lane;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public final class DOIParser {

    /**
     * Extract a DOI from a link; assumes link's hostname will end in .org and DOI's begin with 10.
     *
     * @param link
     *            String containing a DOI
     * @return DOI or empty if no DOI found
     */
    public static String parse(final String link) {
        String s = link;
        if (null != s) {
            try {
                s = URLDecoder.decode(s, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // won't happen
            }
        }
        s = s.replaceFirst(".*\\.org/", "");
        s = s.trim();
        // http://www.doi.org/doi_handbook/2_Numbering.html#2.6.3
        if (s.startsWith("doi:")) {
            s = s.substring(4);
        }
        if (s.startsWith("10.") || isShortDoi(s)) {
            return s;
        }
        return "";
    }

    /**
     * http://www.doi.org/doi_handbook/2_Numbering.html#2.10
     *
     * @param doi
     * @return true if DOI appears to be shortened (letters and numbers but not just numbers)
     */
    private static boolean isShortDoi(final String doi) {
        return doi.matches("\\w+") && !doi.matches("\\d+");
    }
}
