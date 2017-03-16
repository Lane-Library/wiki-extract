package edu.stanford.lane.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * simple utility to print out strings (DOIs) from file1 not found in file2
 *
 * @author ryanmax
 */
public class Checker {

    private static final Logger LOG = LoggerFactory.getLogger(Checker.class);

    public Checker(final String inputFile1, final String inputFile2) {
        Set<String> strings1 = new HashSet<>();
        Set<String> strings2 = new HashSet<>();
        File in = new File(inputFile1);
        if (in.exists()) {
            strings1 = extract(in);
        }
        in = new File(inputFile2);
        if (in.exists()) {
            strings2 = extract(in);
        }
        for (String doi : strings1) {
            if (!strings2.contains(doi)) {
                System.out.println(doi);
            }
        }
    }

    public static void main(final String[] args) {
        new Checker(args[0], args[1]);
    }

    private Set<String> extract(final File input) {
        Set<String> strings = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(input));) {
            String line;
            while ((line = br.readLine()) != null) {
                strings.add(line);
            }
        } catch (IOException e) {
            LOG.error("can't read file", e);
        }
        return strings;
    }
}
