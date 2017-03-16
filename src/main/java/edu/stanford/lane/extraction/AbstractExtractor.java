package edu.stanford.lane.extraction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ryanmax
 */
public abstract class AbstractExtractor {

    protected static final char RETURN = '\n';

    protected static final char TAB = '\t';

    private static final int FIVE_SECONDS = 5000;

    private static final Logger LOG = LoggerFactory.getLogger(AbstractExtractor.class);

    protected String getContent(final String link) {
        StringBuilder content = new StringBuilder();
        URL url = null;
        try {
            url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream is = null;
            int status = connection.getResponseCode();
            if (HttpURLConnection.HTTP_OK == status) {
                is = connection.getInputStream();
            } else if (HttpURLConnection.HTTP_UNAVAILABLE == status) {
                LOG.info("503 ... retrying request");
                Thread.sleep(FIVE_SECONDS);
                return getContent(link);
            } else {
                LOG.info("response status: {}", status);
                LOG.info("can't fetch data for url: {}", url);
                is = connection.getErrorStream();
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    content.append(inputLine);
                }
            }
        } catch (IOException | InterruptedException e) {
            LOG.info("can't fetch data for url: {}", url, e);
        }
        return content.toString();
    }
}
