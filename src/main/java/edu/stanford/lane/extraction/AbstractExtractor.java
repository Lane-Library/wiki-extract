package edu.stanford.lane.extraction;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author ryanmax
 */
public abstract class AbstractExtractor {

    protected static final char RETURN = '\n';

    protected static final char TAB = '\t';

    private static final int FIVE_SECONDS = 5000;

    private static final Logger LOG = LoggerFactory.getLogger(AbstractExtractor.class);

    private DocumentBuilderFactory factory;

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

    protected Document xmlToDocument(final String xmlContent) {
        if (null == this.factory) {
            this.factory = DocumentBuilderFactory.newInstance();
        }
        Document doc = null;
        try {
            this.factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            doc = this.factory.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
        } catch (SAXException | IOException | ParserConfigurationException e) {
            LOG.error("failed to parse xml: {}", xmlContent, e);
        }
        return doc;
    }
}
