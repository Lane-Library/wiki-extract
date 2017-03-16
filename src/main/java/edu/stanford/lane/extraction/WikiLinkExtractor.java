package edu.stanford.lane.extraction;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.stanford.lane.WikiExtractException;

/**
 * extract external links from Wikipedia; assumes pages.obj is present (i.e. run WikiPageExtractor first)
 *
 * @author ryanmax
 */
public class WikiLinkExtractor extends AbstractExtractor implements Extractor {

    // as is, query fetches http and protocol-less links; add &euprotocol=https to query for https and protocol-less
    private static final String BASE_URL = ".wikipedia.org/w/api.php?action=query&list=exturlusage&format=xml&euprop=ids%7Ctitle%7Curl&eulimit=5000";

    private static final Logger LOG = LoggerFactory.getLogger(WikiLinkExtractor.class);

    private static final String PROTOCOL = "https://";

    private String euquery;

    private DocumentBuilderFactory factory;

    private List<String> languages = new ArrayList<>();

    private String namespace;

    private String path;

    private Set<String> projectMedicinePages;

    private XPath xpath;

    public WikiLinkExtractor(final String euquery, final String outputPath, final String languages,
            final String namespace) {
        this.euquery = euquery;
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
        LOG.info(" - start - link extraction");
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("pages.obj"))) {
            this.projectMedicinePages = (Set<String>) ois.readObject();
        } catch (IOException e) {
            LOG.error("missing pages.obj file ... can't determine wikiProjectMedicine status", e);
            this.projectMedicinePages = new HashSet<>();
        } catch (ClassNotFoundException e) {
            throw new WikiExtractException(e);
        }
        LOG.info("euquery: " + this.euquery);
        LOG.info("path: " + this.path);
        LOG.info("namespace: " + this.namespace);
        LOG.info("languages: " + this.languages);
        for (String lang : this.languages) {
            extract(lang);
        }
        LOG.info(" - end - link extraction");
    }

    public void extract(final String lang) {
        String query = PROTOCOL + lang + BASE_URL + "&euquery=" + this.euquery + "&eunamespace=" + this.namespace;
        File directory = new File(this.path + "/" + lang);
        if (!directory.exists()) {
            directory.mkdir();
        }
        File outFile = new File(directory.getAbsolutePath() + "/out.txt");
        try (FileOutputStream outFos = new FileOutputStream(outFile)) {
            outFile.createNewFile();
            boolean more = true;
            int offset = 0;
            while (more) {
                String xmlContent = getContent(query + "&euoffset=" + offset);
                Document doc;
                NodeList euNodes = null;
                try {
                    this.factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
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
                    LOG.error("failed to fetch data", e);
                }
            }
        } catch (IOException e) {
            LOG.error("can't write outFile", e);
            throw new WikiExtractException(e);
        }
    }

    private void maybeWriteLine(final String lang, final Element el, final FileOutputStream fos) throws IOException {
        String url = el.getAttribute("url");
        String ns = el.getAttribute("ns");
        String title = el.getAttribute("title");
        boolean isProjectMed = this.projectMedicinePages.contains(ns + ":::" + title);
        StringBuilder sb = new StringBuilder();
        sb.append(lang);
        sb.append(TAB).append(el.getAttribute("pageid"));
        sb.append(TAB).append(ns);
        sb.append(TAB).append(isProjectMed);
        sb.append(TAB).append(title);
        sb.append(TAB).append(url);
        sb.append(RETURN);
        fos.write(sb.toString().getBytes());
    }
}
