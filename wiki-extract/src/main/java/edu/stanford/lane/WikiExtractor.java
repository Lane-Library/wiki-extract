package edu.stanford.lane;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author ryanmax
 */
public class WikiExtractor implements Extractor {

    private static final String BASE_URL = ".wikipedia.org/w/api.php?action=query&list=exturlusage&format=xml&euprop=ids%7Ctitle%7Curl&eulimit=500";

    private static final RequestConfig HTTP_CONFIG = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES)
            .build();

    private static final HttpClient httpClient = HttpClients.createDefault();

    private static final String PROTOCOL = "https://";

    private String euquery;

    private DocumentBuilderFactory factory;

    private boolean filter = true;

    private List<String> languages = new ArrayList<>();

    private Logger log = LoggerFactory.getLogger(getClass());

    private String namespace;

    private String path;

    private String urlFilter;

    private XPath xpath;

    public WikiExtractor(final String euquery, final String urlFilter, final String outputPath, final String languages,
            final String namespace) {
        this.euquery = euquery;
        this.urlFilter = urlFilter;
        if (null == this.urlFilter || this.urlFilter.isEmpty()) {
            this.filter = false;
        }
        this.namespace = namespace;
        for (String l : languages.split(",")) {
            this.languages.add(l);
        }
        this.factory = DocumentBuilderFactory.newInstance();
        this.xpath = XPathFactory.newInstance().newXPath();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String[] pathDirs = { outputPath, outputPath + "/" + date };
        for (String dir : pathDirs) {
            File directory = new File(dir);
            if (!directory.exists()) {
                directory.mkdir();
            }
        }
        this.path = outputPath + "/" + date;
    }

    @Override
    public void extract() {
        for (String lang : this.languages) {
            extract(lang);
        }
    }

    public void extract(final String lang) {
        String query = PROTOCOL + lang + BASE_URL + "&euquery=" + this.euquery + "&eunamespace=" + this.namespace;
        File directory = new File(this.path + "/" + lang);
        if (!directory.exists()) {
            directory.mkdir();
        }
        try {
            File outFile = new File(directory.getAbsolutePath() + "/out.txt");
            FileOutputStream outFos = null;
            outFile.createNewFile();
            outFos = new FileOutputStream(outFile);
            boolean more = true;
            int offset = 0;
            while (more) {
                String xmlContent = getContent(query + "&euoffset=" + offset);
                Document doc;
                NodeList euNodes = null;
                try {
                    doc = this.factory.newDocumentBuilder().parse(new ByteArrayInputStream(xmlContent.getBytes()));
                    NodeList continueNode = (NodeList) this.xpath.evaluate("/api/continue", doc,
                            XPathConstants.NODESET);
                    if (continueNode.getLength() == 0) {
                        more = false;
                    } else {
                        offset = Integer.parseInt(
                                (String) this.xpath.evaluate("/api/continue/@euoffset", doc, XPathConstants.STRING));
                    }
                    euNodes = (NodeList) this.xpath.evaluate("/api/query/exturlusage/eu", doc, XPathConstants.NODESET);
                    for (int n = 0; n < euNodes.getLength(); n++) {
                        Element el = (Element) euNodes.item(n);
                        maybeWriteLine(lang, el, outFos);
                    }
                } catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException e) {
                    this.log.error("failed to fetch data", e);
                }
            }
            outFos.close();
        } catch (IOException e) {
            this.log.error(e.getMessage(), e);
            throw new WikiExtractException(e);
        }
    }

    private String getContent(final String url) {
        String htmlContent = null;
        HttpResponse res = null;
        HttpGet method = new HttpGet(url);
        method.setConfig(HTTP_CONFIG);
        try {
            res = WikiExtractor.httpClient.execute(method);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                htmlContent = EntityUtils.toString(res.getEntity());
            }
        } catch (Exception e) {
            this.log.error(e.getMessage(), e);
            method.abort();
        }
        return htmlContent;
    }

    private void maybeWriteLine(final String lang, final Element el, final FileOutputStream fos) throws IOException {
        String url = el.getAttribute("url");
        if (!this.filter || url.contains(this.urlFilter)) {
            StringBuilder sb = new StringBuilder();
            sb.append(lang);
            sb.append("\t" + el.getAttribute("pageid"));
            sb.append("\t" + el.getAttribute("ns"));
            sb.append("\t" + el.getAttribute("title"));
            sb.append("\t" + url);
            sb.append("\n");
            fos.write(sb.toString().getBytes());
        }
    }
}
