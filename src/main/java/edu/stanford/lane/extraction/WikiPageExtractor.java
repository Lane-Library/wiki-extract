package edu.stanford.lane.extraction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.stanford.lane.WikiExtractException;

/**
 * extract a list of Wikipedia pages that are children of one or more Wikipedia category; writes them to a file
 * pages.obj used by WikiLinkExtractor
 *
 * @author ryanmax
 */
public class WikiPageExtractor extends AbstractExtractor implements Extractor {

    private static final String BASE_URL = "https://en.wikipedia.org/w/api.php?action=query&format=xml&list=categorymembers&cmlimit=5000&cmtitle=";

    private static final Logger LOG = LoggerFactory.getLogger(WikiPageExtractor.class);

    private List<String> categories = new ArrayList<>();

    private HashSet<String> pages = new HashSet<>();

    private String path;

    private XPath xpath;

    public WikiPageExtractor(final String outputPath, final String categories) {
        for (String l : categories.split(",")) {
            this.categories.add(l);
        }
        this.xpath = XPathFactory.newInstance().newXPath();
        this.path = outputPath;
        try {
            Files.createDirectories(Paths.get(this.path));
        } catch (IOException e) {
            LOG.error("can't create directory {}", this.path, e);
        }
    }

    @Override
    public void extract() {
        LOG.info(" - start - page extraction");
        LOG.info("path: {}", this.path);
        LOG.info("categories: {}", this.categories);
        for (String cat : this.categories) {
            extract(cat.trim());
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("pages.obj"))) {
            oos.writeObject(this.pages);
        } catch (IOException e) {
            LOG.error("error writing pages object", e);
        }
        LOG.info(" - end - page extraction");
    }

    public void extract(final String cat) {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        try (FileOutputStream fw = new FileOutputStream(new File(this.path + "/" + date + "-out.txt"), true)) {
            String query = BASE_URL + "&cmtitle=" + URLEncoder.encode(cat, StandardCharsets.UTF_8.name());
            boolean more = true;
            String cmcontinue = "";
            while (more) {
                String xmlContent = getContent(
                        query + "&cmcontinue=" + URLEncoder.encode(cmcontinue, StandardCharsets.UTF_8.name()));
                Document doc = xmlToDocument(xmlContent);
                NodeList cmNodes = null;
                more = moreToParse(doc);
                cmcontinue = (String) doXpath("/api/continue/@cmcontinue", doc, XPathConstants.STRING);
                cmNodes = (NodeList) doXpath("/api/query/categorymembers/cm", doc, XPathConstants.NODESET);
                for (int n = 0; n < cmNodes.getLength(); n++) {
                    Element el = (Element) cmNodes.item(n);
                    StringBuilder sb = new StringBuilder();
                    String ns = el.getAttribute("ns");
                    String title = el.getAttribute("title");
                    sb.append(cat);
                    sb.append(TAB).append(ns);
                    sb.append(TAB).append(title);
                    sb.append(RETURN);
                    writeLine(fw, cat, ns, title);
                    if ("1".equals(ns)) {
                        // if need pageid, need another API call, 500 titles at a time for bots:
                        // /w/api.php?action=query&format=json&prop=pageprops&titles=Fungus%7CGenetics
                        writeLine(fw, cat, "0", title.replaceFirst("^Talk:", ""));
                    }
                    fw.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (IOException e) {
            LOG.error("error fetching pages", e);
            throw new WikiExtractException(e);
        }
    }

    private Object doXpath(final String expression, final Document doc, final QName qname) {
        Object o = null;
        try {
            o = this.xpath.evaluate(expression, doc, qname);
        } catch (XPathExpressionException e) {
            LOG.error("xpath error", e);
        }
        return o;
    }

    private boolean moreToParse(final Document doc) {
        NodeList continueNode = null;
        try {
            continueNode = (NodeList) this.xpath.evaluate("/api/continue/@cmcontinue", doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            LOG.error("error determining if more content to parse", e);
        }
        if (null != continueNode && continueNode.getLength() != 0) {
            return true;
        }
        return false;
    }

    private void writeLine(final FileOutputStream fw, final String cat, final String ns, final String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(cat);
        sb.append(TAB).append(ns);
        sb.append(TAB).append(title);
        sb.append(RETURN);
        try {
            fw.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOG.error("error writing", e);
        }
        this.pages.add(ns + ":::" + title);
    }
}
