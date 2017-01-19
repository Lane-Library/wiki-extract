package edu.stanford.lane;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.lane.WikiLinkExtractor.Category;

/**
 * @author ryanmax
 */
public class Summarizer {

    private Set<String> dois = new HashSet<>();

    private Logger log = LoggerFactory.getLogger(getClass());

    private Set<String> nonProjectMedicineDois = new HashSet<>();

    private Set<String> projectMedicineDois = new HashSet<>();

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
     * DOI
     * </pre>
     */
    public Summarizer(final String[] inputFiles) {
        for (String file : inputFiles) {
            File in = new File(file);
            if (in.exists()) {
                extract(in);
            }
        }
        try {
            writeDoiOutput("summary.txt");
        } catch (IOException e) {
            this.log.error("can't write output", e);
        }
    }

    public static void main(final String[] args) {
        new Summarizer(args);
    }

    private void extract(final File input) {
        try (BufferedReader br = new BufferedReader(new FileReader(input));) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\t");
                String isProjectMedPage = fields[3];
                String doi = fields[5];
                doi = DOIParser.parse(doi);
                if (null != doi && !doi.isEmpty()) {
                    this.dois.add(doi);
                }
                if ("true".equalsIgnoreCase(isProjectMedPage)) {
                    this.projectMedicineDois.add(doi);
                } else {
                    this.nonProjectMedicineDois.add(doi);
                }
            }
        } catch (IOException e) {
            this.log.error("can't read file", e);
        }
    }

    private void writeDoiOutput(final String path) throws IOException {
        File doiOutFile = new File(path);
        doiOutFile.createNewFile();
        FileOutputStream doiOutFos = new FileOutputStream(doiOutFile);
        for (String doi : this.dois) {
            Category cat = Category.UNKOWN;
            if (this.nonProjectMedicineDois.contains(doi) && this.projectMedicineDois.contains(doi)) {
                cat = Category.CAT_3_BOTH_PROJECT_MED_AND_NON_PROJECT_MED;
            } else if (this.nonProjectMedicineDois.contains(doi)) {
                cat = Category.CAT_2_ONLY_NON_PROJECT_MED;
            } else if (this.projectMedicineDois.contains(doi)) {
                cat = Category.CAT_1_ONLY_PROJECT_MED;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(doi);
            sb.append("\t" + cat);
            sb.append("\n");
            doiOutFos.write(sb.toString().getBytes());
        }
        doiOutFos.close();
    }
}
