package edu.stanford.lane;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

public final class DOIParser {

    /**
     * Extract a DOI from a link; assumes link's hostname will end in .org and DOI's begin with 10. or looks "shortened"
     *
     * @param link
     *            String containing a DOI
     * @return DOI or empty if no DOI found
     */
    public static String parse(final String link) {
        String parsed = link;
        if (null != parsed) {
            try {
                parsed = URLDecoder.decode(parsed, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // won't happen
            }
        }
        parsed = removePrefixes(parsed);
        if (isShortDoi(parsed)) {
            parsed = resolveShortenedDoi(parsed);
        }
        parsed = parsed.toLowerCase().trim();
        if (parsed.startsWith("10.")) {
            return parsed;
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

    private static String removePrefixes(final String doi) {
        String s = doi;
        s = s.replaceFirst("(?i).*\\.org/", "");
        // http://www.doi.org/doi_handbook/2_Numbering.html#2.6.3
        s = s.replaceFirst("(?i)^doi:", "");
        return s;
    }

    private static String resolveShortenedDoi(final String doi) {
        URL url = null;
        URLConnection connection = null;
        String resolvedDoi = removePrefixes(doi);
        try {
            url = new URL("http://doi.org/" + doi);
            connection = url.openConnection();
            ((HttpURLConnection) connection).setInstanceFollowRedirects(false);
        } catch (IOException e) {
            throw new WikiExtractException("can't fetch doi: " + url, e);
        }
        String location = connection.getHeaderField("Location");
        if (null != location) {
            resolvedDoi = location;
        }
        return removePrefixes(resolvedDoi);
    }
}
