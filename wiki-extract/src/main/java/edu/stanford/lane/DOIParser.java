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
        if (s.startsWith("10.")) {
            return s.trim();
        }
        return "";
    }
}
