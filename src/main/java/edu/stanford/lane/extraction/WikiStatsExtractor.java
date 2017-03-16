package edu.stanford.lane.extraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * extract pageview stats from Wikipedia; note that dates, English language, access and agent values are all hard-coded
 * and specific to this project
 *
 * @author ryanmax
 */
public class WikiStatsExtractor extends AbstractExtractor implements Extractor {

    // project: en.wikipedia.org
    // access: all-access (desktop, mobile-app, mobile-web)
    // agent: user (NOT spider or bot)
    private static final String BASE_URL = "https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/en.wikipedia.org/all-access/user/{page}/daily/20160801/20160831";

    private String endDate;

    private String inputFile;

    private Logger log = LoggerFactory.getLogger(getClass());

    private ObjectMapper mapper = new ObjectMapper();

    private String startDate;

    public WikiStatsExtractor(final String inputFile, final String startDate, final String endDate) {
        this.inputFile = inputFile;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public void extract() {
        File in = new File(this.inputFile);
        if (in.exists()) {
            extract(in);
        }
    }

    private void extract(final File input) {
        try (BufferedReader br = new BufferedReader(new FileReader(input));
                FileWriter fw = new FileWriter(this.inputFile + "-out.txt", true);) {
            String line;
            while ((line = br.readLine()) != null) {
                String json = getContent(getUrl(line));
                int views = jsonToStats(json);
                fw.write(line);
                fw.write(TAB);
                fw.write(Integer.toString(views));
                fw.write(TAB);
            }
        } catch (IOException e) {
            this.log.error("can't read/write to extract", e);
        }
    }

    private String getUrl(final String page) {
        String encodedPage = page.replace(" ", "_");
        try {
            encodedPage = URLEncoder.encode(encodedPage, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            this.log.error("won't happen", e);
        }
        String url = BASE_URL.replace("{page}", encodedPage);
        url = url.replace("{startDate}", this.startDate);
        url = url.replace("{endDate}", this.endDate);
        return url;
    }

    private int jsonToStats(final String json) {
        int sum = 0;
        if (null != json && !json.isEmpty()) {
            Map<String, Object> statsData = null;
            try {
                statsData = this.mapper.readValue(json, Map.class);
            } catch (IOException e) {
                this.log.error("error deserializing json", e);
            }
            List<Map<String, Object>> list = (List<Map<String, Object>>) statsData.get("items");
            for (Map<String, Object> map : list) {
                int views = (int) map.get("views");
                sum = sum + views;
            }
        }
        return sum;
    }
}
