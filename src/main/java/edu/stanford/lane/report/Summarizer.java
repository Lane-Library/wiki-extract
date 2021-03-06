package edu.stanford.lane.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.lane.DOIParser;

/**
 * summarize wiki extract data files; re-parse DOI from link_to_doi.org field because parsing changed after data was
 * originally extracted from Wikipedia
 *
 * <pre>
 * ================================================================================
 * legend for YYYY-MM-DD/en/out.txt files:
 * ================================================================================
 * language (all en)
 * pageid (https://en.wikipedia.org/?curid=XXXX to fetch page)
 * namespace (https://en.wikipedia.org/wiki/Wikipedia:Namespace)
 * isProjectMedicinePage
 * page_title
 * link_to_doi.org
 * </pre>
 *
 * @author ryanmax
 */
public final class Summarizer {

    protected enum Category {
        CAT_1_ONLY_PROJECT_MED, CAT_2_ONLY_NON_PROJECT_MED, CAT_3_BOTH_PROJECT_MED_AND_NON_PROJECT_MED, UNKOWN
    }

    private static final Logger LOG = LoggerFactory.getLogger(Summarizer.class);

    private static final char TAB = '\t';

    private Set<String> dois = new HashSet<>();

    private IncrementingHashMap mapNonProjectMedicineDois = new IncrementingHashMap();

    private IncrementingHashMap mapProjectMedicineDois = new IncrementingHashMap();

    private Set<String> uniqueEntries = new HashSet<>();

    private Summarizer() {
        // empty private constructor
    }

    public static void main(final String[] args) {
        Summarizer summarizer = new Summarizer();
        summarizer.summarize(args);
    }

    /**
     * summarize wiki extract data files; re-parse DOI from link_to_doi.org field because parsing changed after data was
     * originally extracted from wikipedia
     *
     * <pre>
     * ================================================================================
     * legend for YYYY-MM-DD/en/out.txt files:
     * ================================================================================
     * language (all en)
     * pageid (https://en.wikipedia.org/?curid=XXXX to fetch page)
     * namespace (https://en.wikipedia.org/wiki/Wikipedia:Namespace)
     * isProjectMedicinePage
     * page_title
     * link_to_doi.org
     * </pre>
     *
     * @param inputFiles
     *            list of input file paths
     */
    public void summarize(final String[] inputFiles) {
        DOIParser doiParser = new DOIParser();
        for (String file : inputFiles) {
            File in = new File(file);
            if (in.exists()) {
                extract(in);
            }
        }
        for (String entry : this.uniqueEntries) {
            String[] fields = entry.split(Character.toString(TAB));
            String isProjectMedPage = fields[3];
            String link = fields[5];
            for (String doi : doiParser.parse(link)) {
                if (null != doi && !doi.isEmpty()) {
                    this.dois.add(doi);
                }
                if ("true".equalsIgnoreCase(isProjectMedPage)) {
                    this.mapProjectMedicineDois.add(doi);
                } else {
                    this.mapNonProjectMedicineDois.add(doi);
                }
            }
        }
        try {
            writeDoiOutput("summary.txt");
        } catch (IOException e) {
            LOG.error("can't write output", e);
        }
    }

    private void extract(final File input) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(input), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                this.uniqueEntries.add(line);
            }
        } catch (IOException e) {
            LOG.error("can't read file", e);
        }
    }

    private void writeDoiOutput(final String path) throws IOException {
        File doiOutFile = new File(path);
        try (FileOutputStream doiOutFos = new FileOutputStream(doiOutFile)) {
            for (String doi : this.dois) {
                Category cat = Category.UNKOWN;
                int count = 0;
                if (this.mapNonProjectMedicineDois.containsKey(doi) && this.mapProjectMedicineDois.containsKey(doi)) {
                    cat = Category.CAT_3_BOTH_PROJECT_MED_AND_NON_PROJECT_MED;
                    count = this.mapNonProjectMedicineDois.get(doi).intValue()
                            + this.mapProjectMedicineDois.get(doi).intValue();
                } else if (this.mapNonProjectMedicineDois.containsKey(doi)) {
                    cat = Category.CAT_2_ONLY_NON_PROJECT_MED;
                    count = this.mapNonProjectMedicineDois.get(doi).intValue();
                } else if (this.mapProjectMedicineDois.containsKey(doi)) {
                    cat = Category.CAT_1_ONLY_PROJECT_MED;
                    count = this.mapProjectMedicineDois.get(doi).intValue();
                }
                StringBuilder sb = new StringBuilder();
                sb.append(doi);
                sb.append(TAB).append(cat);
                sb.append(TAB).append(count);
                sb.append('\n');
                doiOutFos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
