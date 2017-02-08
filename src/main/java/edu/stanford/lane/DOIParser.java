package edu.stanford.lane;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DOIParser {

    private static ObjectMapper mapper = new ObjectMapper();

    private static final String OBJECT_STORE = "parsed-dois.obj";

    private static final int TEN_SECS = 1000 * 10;

    private Logger log = LoggerFactory.getLogger(getClass());

    private Map<String, List<String>> parsedDois;

    public DOIParser() {
        // empty
    }

    /**
     * http://www.doi.org/doi_handbook/2_Numbering.html#2.10
     *
     * @param doi
     * @return true if DOI appears to be shortened (letters and numbers but not just numbers)
     */
    private static boolean isShortDoi(final String doi) {
        return doi.matches("\\w+") && !doi.matches("\\d+");
    }

    private static String removePrefixes(final String doi) {
        String s = doi;
        s = s.replaceFirst("(?i).*\\.org/", "");
        // http://www.doi.org/doi_handbook/2_Numbering.html#2.6.3
        s = s.replaceFirst("(?i)^doi:\\s?", "");
        return s;
    }

    private static String resolveShortenedDoi(final String doi) {
        URL url = null;
        URLConnection connection = null;
        String resolvedDoi = doi;
        try {
            url = new URL("http://doi.org/" + resolvedDoi);
            connection = url.openConnection();
            ((HttpURLConnection) connection).setInstanceFollowRedirects(false);
        } catch (IOException e) {
            throw new WikiExtractException("can't fetch doi: " + url, e);
        }
        String location = connection.getHeaderField("Location");
        if (null != location) {
            resolvedDoi = removePrefixes(location);
        }
        return resolvedDoi;
    }

    /**
     * Extract a DOI from a link; assumes link's hostname will end in .org and DOI's begin with 10. or looks "shortened"
     *
     * @param link
     *            String containing a DOI
     * @return DOI or empty if no DOI found
     */
    public List<String> parse(final String link) {
        cacheMaintenance();
        if (this.parsedDois.containsKey(link)) {
            return this.parsedDois.get(link);
        }
        List<String> dois = new ArrayList<>();
        if (null != link) {
            String json = fetchDoiData(removePrefixes(link));
            String handle = jsonToCannonicalDoi(json);
            if (null != handle) {
                if (!isShortDoi(handle)) {
                    dois.add(handle);
                    dois.addAll(jsonToAlias(json));
                } else {
                    dois.add(resolveShortenedDoi(handle));
                }
            }
        }
        List<String> normalized = new ArrayList<>();
        for (String doi : dois) {
            if (null != doi && doi.startsWith("10.")) {
                doi = doi.toLowerCase().trim();
                normalized.add(doi);
            }
        }
        this.parsedDois.put(link, normalized);
        return this.parsedDois.get(link);
    }

    private void cacheMaintenance() {
        File objFile = new File(OBJECT_STORE);
        if (null == this.parsedDois) {
            if (!objFile.exists()) {
                this.parsedDois = new HashMap<>();
            } else {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(objFile))) {
                    this.parsedDois = (Map<String, List<String>>) ois.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    this.log.error("can't load parsed DOI object file", e);
                }
            }
        } else if (this.parsedDois.size() % 1000 == 0
                && objFile.lastModified() < System.currentTimeMillis() - TEN_SECS) {
            this.log.debug("writing new parsed DOI object file with # of dois: " + this.parsedDois.size());
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(OBJECT_STORE))) {
                oos.writeObject(this.parsedDois);
            } catch (IOException e) {
                this.log.error("can't write parsed DOI object file", e);
            }
        }
    }

    private String fetchDoiData(final String doi) {
        StringBuilder json = new StringBuilder();
        URL url = null;
        try {
            url = new URL("http://doi.org/api/handles/" + doi);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream is = null;
            if (200 == connection.getResponseCode()) {
                is = connection.getInputStream();
            } else {
                is = connection.getErrorStream();
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    json.append(inputLine);
                }
            }
        } catch (IOException e) {
            this.log.info("can't fetch data for doi: " + url);
        }
        return json.toString();
    }

    @SuppressWarnings("unchecked")
    private List<String> jsonToAlias(final String json) {
        List<String> aliases = new ArrayList<>();
        if (null != json && !json.isEmpty()) {
            Map<String, Object> data = null;
            try {
                data = mapper.readValue(json, Map.class);
            } catch (IOException e) {
                this.log.error("can't fetch aliases from json : " + json, e);
            }
            if (data.containsKey("values")) {
                List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("values");
                for (Map<String, Object> map : list) {
                    String type = (String) map.get("type");
                    if ("HS_ALIAS".equalsIgnoreCase(type)) {
                        Map<String, String> dataMap = (Map<String, String>) map.get("data");
                        aliases.add(dataMap.get("value"));
                    }
                }
            }
        }
        return aliases;
    }

    @SuppressWarnings("unchecked")
    private String jsonToCannonicalDoi(final String json) {
        String handle = null;
        if (null != json && !json.isEmpty()) {
            Map<String, String> data = null;
            try {
                data = mapper.readValue(json, Map.class);
            } catch (IOException e) {
                this.log.error("can't fetch data from json : " + json, e);
            }
            handle = data.get("handle");
        }
        return handle;
    }
}
