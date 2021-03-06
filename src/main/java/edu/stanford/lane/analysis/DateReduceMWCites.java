package edu.stanford.lane.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 * utility used to reduce mwcites data set to DOIs found on or before end of 2016-08-31 so as to compare mwcites with our extraction approach
 * mwcites dataset: https://figshare.com/articles/Wikipedia_Scholarly_Article_Citations/1299540/9
 * python-mwcites: https://github.com/mediawiki-utilities/python-mwcites
 * </pre>
 *
 * <pre>
 * from 2017-02-09 email from ryanmax:
 * Analysis: I reviewed Aaron Halfaker's mwcites project. The DOI parser looks solid, although our extraction and this one differ in some significant ways:
 * - we limit extraction to external links; mwcites pulls identifiers from free text occurring in Wikipedia pages as well as links
 * - mwcites limits extraction to wikipedia articles (namespace=0); we include all pages in all namespaces (User, Talk, etc.) that have DOI links
 * - mwcites does not seem to handle shortened DOIs (http://shortdoi.org/); this is likely a small number
 *
 * I reduced the most recent output from mwcites (https://figshare.com/articles/Wikipedia_Scholarly_Article_Citations/1299540/9) to those DOIs with a revision timestamp before 2016-09-01.
 *
 * == Results ==
 * mwcites data:
 * 972,948 unique page/DOI combos
 * 688,374 unique DOIs
 * CrossRef dataset DOIs that are missing in this dataset: 2,980
 *
 * our data:
 * 1,046,336 unique page/doi combos (2016-08-31 file)
 * 687,294 unique DOIs (all 2016-08 files)
 * CrossRef dataset DOIs that are missing in this dataset: 601
 *
 * The analysis above validates our approach.
 * </pre>
 *
 * @author ryanmax
 */
public final class DateReduceMWCites {

    private static final Instant end = Instant.parse("2016-09-01T00:00:00Z");

    private static final Logger LOG = LoggerFactory.getLogger(DateReduceMWCites.class);

    private DateReduceMWCites() {
        // empty private constructor
    }

    public static void main(final String[] args) {
        if (args.length != 1) {
            LOG.error("expected path as only argument");
        }
        DateReduceMWCites r = new DateReduceMWCites();
        r.reduce(args[0]);
    }

    private void reduce(final String inputFile) {
        File in = new File(inputFile);
        if (in.exists()) {
            File outfile = new File(inputFile + "-out.txt");
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(in), StandardCharsets.UTF_8));
                    FileOutputStream outFos = new FileOutputStream(outfile)) {
                String line;
                String idType;
                String timestamp;
                while ((line = br.readLine()) != null) {
                    String[] fields = line.split("\t");
                    idType = fields[4];
                    if ("doi".equals(idType)) {
                        timestamp = fields[3];
                        Instant i = Instant.parse(timestamp);
                        if (i.isBefore(end)) {
                            outFos.write((line + "\n").getBytes(StandardCharsets.UTF_8));
                        }
                    }
                }
            } catch (IOException e) {
                LOG.error("can't read file", e);
            }
        }
    }
}
