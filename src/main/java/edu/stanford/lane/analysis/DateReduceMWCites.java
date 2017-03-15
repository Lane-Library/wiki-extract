package edu.stanford.lane.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ryanmax
 *
 *         <pre>
 * used to reduce mwcites data set to DOIs found on or before end of 2016-08-31
 * mwcites dataset: https://figshare.com/articles/Wikipedia_Scholarly_Article_Citations/1299540/9
 * python-mwcites: https://github.com/mediawiki-utilities/python-mwcites
 *         </pre>
 */
public class DateReduceMWCites {

    private static final Instant end = Instant.parse("2016-09-01T00:00:00Z");

    private Logger log = LoggerFactory.getLogger(getClass());

    public DateReduceMWCites(final String inputFile) throws IOException {
        File in = new File(inputFile);
        if (in.exists()) {
            File outfile = new File(inputFile + "-out.txt");
            outfile.createNewFile();
            FileOutputStream outFos = new FileOutputStream(outfile);
            try (BufferedReader br = new BufferedReader(new FileReader(in))) {
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
                            outFos.write((line + "\n").getBytes());
                        }
                    }
                }
            } catch (IOException e) {
                this.log.error("can't read file", e);
            }
            outFos.close();
        }
    }

    public static void main(final String[] args) throws IOException {
        new DateReduceMWCites("/path/to/mwcites/data/doi_isbn_pubmed_and_arxiv.enwiki-20161201.tsv");
    }
}
