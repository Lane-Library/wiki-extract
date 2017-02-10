package edu.stanford.lane.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ryanmax
 */
public class URLDecodeFile {

    private int fieldToEncode;

    private String inputFile;

    private Logger log = LoggerFactory.getLogger(getClass());

    /*
     * used this to URL decode DOIs from CrossRef (Joe Wass) assumes tab-delimited input
     */
    public URLDecodeFile(final String inputFile, final int fieldToEncode) {
        this.inputFile = inputFile;
        this.fieldToEncode = fieldToEncode;
        File in = new File(this.inputFile);
        if (in.exists()) {
            extract(in);
        }
    }

    public static void main(final String[] args) {
        new URLDecodeFile(args[0], Integer.parseInt(args[1]));
    }

    private void extract(final File input) {
        try (BufferedReader br = new BufferedReader(new FileReader(input));
                FileWriter fw = new FileWriter(this.inputFile + "-decoded.txt", false);) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\t");
                try {
                    fields[this.fieldToEncode] = URLDecoder.decode(fields[this.fieldToEncode], "UTF-8");
                } catch (IllegalArgumentException e) {
                    this.log.error("can't decode: " + fields[this.fieldToEncode], e);
                }
                StringBuilder sb = new StringBuilder();
                for (String field : fields) {
                    sb.append(field);
                    sb.append("\t");
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.append("\n");
                fw.write(sb.toString());
            }
        } catch (IOException e) {
            this.log.error(e.getMessage(), e);
        }
    }
}
