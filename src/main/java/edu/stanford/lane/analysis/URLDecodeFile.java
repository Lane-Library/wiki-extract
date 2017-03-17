package edu.stanford.lane.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * simple utility used to URL decode DOIs from CrossRef (Joe Wass) assumes tab-delimited input; first argument is DOI
 * field index
 *
 * @author ryanmax
 */
public class URLDecodeFile {

    private static final Logger LOG = LoggerFactory.getLogger(URLDecodeFile.class);

    public URLDecodeFile() {
        // empty private constructor
    }

    public static void main(final String[] args) {
        decode(args[0], Integer.parseInt(args[1]));
    }

    private static void decode(final String inputFile, final int fieldToEncode) {
        File input = new File(inputFile);
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(input), StandardCharsets.UTF_8));
                FileOutputStream outFos = new FileOutputStream(new File(input.getAbsolutePath() + "-decoded.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\t");
                fields[fieldToEncode] = doDecode(fields[fieldToEncode]);
                StringBuilder sb = new StringBuilder();
                for (String field : fields) {
                    sb.append(field);
                    sb.append('\t');
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.append('\n');
                outFos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            LOG.error("can't read/write to extract", e);
        }
    }

    private static String doDecode(final String string) {
        String decoded = string;
        try {
            decoded = URLDecoder.decode(decoded, StandardCharsets.UTF_8.name());
        } catch (IllegalArgumentException | UnsupportedEncodingException e) {
            LOG.error("can't decode: {}", decoded, e);
        }
        return decoded;
    }
}
