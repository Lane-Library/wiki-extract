package edu.stanford.lane.extraction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ryanmax
 */
public abstract class AbstractExtractor {

    private static final int FIVE_SECONDS = 5000;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected String getContent(final String link) {
        StringBuilder content = new StringBuilder();
        URL url = null;
        try {
            url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream is = null;
            int status = connection.getResponseCode();
            if (200 == status) {
                is = connection.getInputStream();
            } else if (503 == status) {
                this.log.info("503 ... retrying request");
                Thread.sleep(FIVE_SECONDS);
                return getContent(link);
            } else {
                this.log.info("response status: " + status);
                this.log.info("can't fetch data for url: " + url);
                is = connection.getErrorStream();
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    content.append(inputLine);
                }
            }
        } catch (IOException | InterruptedException e) {
            this.log.info("can't fetch data for url: " + url, e);
        }
        return content.toString();
    }
}
