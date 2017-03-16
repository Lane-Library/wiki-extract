package edu.stanford.lane;

import java.util.List;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.stanford.lane.extraction.Extractor;

public class Main {

    private List<Extractor> extractors;

    public static void main(final String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "edu/stanford/lane/" + args[0]);
        Main loader = (Main) context.getBean("main");
        try {
            loader.run();
        } finally {
            context.close();
        }
    }

    public void run() {
        if (null != this.extractors) {
            for (Extractor extractor : this.extractors) {
                extractor.extract();
            }
        }
    }

    public void setExtractors(final List<Extractor> extractors) {
        this.extractors = extractors;
    }
}
