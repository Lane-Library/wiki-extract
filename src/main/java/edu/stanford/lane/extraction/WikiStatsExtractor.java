package edu.stanford.lane.extraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * extract pageview stats from Wikipedia; note that English language and access params are hard-coded and specific to
 * this project
 *
 * @author ryanmax
 */
public class WikiStatsExtractor extends AbstractExtractor implements Extractor {

    // https://tools.wmflabs.org/pageviews/url_structure/
    // project: en.wikipedia.org
    // access: all-access (desktop, mobile-app, mobile-web)
    // agent: One of user (human viewer, default), spider (search engine crawlers), bot (WMF bots) or all-agents (user,
    // spider and bot)
    private static final String BASE_URL = "https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/en.wikipedia.org/all-access/{agent}/{page}/daily/{startDate}/{endDate}";

    private static final Logger LOG = LoggerFactory.getLogger(WikiStatsExtractor.class);

    private String agent;

    private String endDate;

    private String inputFile;

    private ObjectMapper mapper = new ObjectMapper();

    private String startDate;

    public WikiStatsExtractor(final String inputFile, final String agent, final String startDate,
            final String endDate) {
        this.inputFile = inputFile;
        this.agent = agent;
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
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(input), StandardCharsets.UTF_8));
                FileOutputStream fw = new FileOutputStream(new File(this.inputFile + "-out.txt"), true)) {
            String line;
            while ((line = br.readLine()) != null) {
                String json = getContent(getUrl(line));
                int views = jsonToStats(json);
                fw.write(line.getBytes(StandardCharsets.UTF_8));
                fw.write(TAB);
                fw.write(Integer.toString(views).getBytes(StandardCharsets.UTF_8));
                fw.write(RETURN);
            }
        } catch (IOException e) {
            LOG.error("can't read/write to extract", e);
        }
    }

    private String getUrl(final String page) {
        String encodedPage = page.replace(' ', '_');
        try {
            encodedPage = URLEncoder.encode(encodedPage, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            LOG.error("won't happen", e);
        }
        String url = BASE_URL.replace("{page}", encodedPage);
        url = url.replace("{agent}", this.agent);
        url = url.replace("{startDate}", this.startDate);
        return url.replace("{endDate}", this.endDate);
    }

    private int jsonToStats(final String json) {
        int sum = 0;
        if (null != json && !json.isEmpty()) {
            Map<String, Object> statsData = null;
            try {
                statsData = this.mapper.readValue(json, Map.class);
            } catch (IOException e) {
                LOG.error("error deserializing json", e);
            }
            if (null != statsData && null != statsData.get("items")) {
                List<Map<String, Object>> list = (List<Map<String, Object>>) statsData.get("items");
                for (Map<String, Object> map : list) {
                    int views = (int) map.get("views");
                    sum = sum + views;
                }
            }
        }
        return sum;
    }
}
