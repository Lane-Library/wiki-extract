package edu.stanford.lane;

import java.io.IOException;
import java.util.List;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import edu.stanford.lane.extraction.Extractor;

public class Main {

    private List<Extractor> extractors;

    public static void main(final String[] args) throws IOException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "edu/stanford/lane/" + args[0] + ".xml");
        Main loader = (Main) context.getBean("main");
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) context.getBean("executor");
        try {
            loader.run();
        } finally {
            executor.shutdown();
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
