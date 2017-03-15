package edu.stanford.lane.extraction;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * @author ryanmax
 */
public class WikiPageExtractor extends AbstractExtractor implements Extractor {

    private static final String BASE_URL = "https://en.wikipedia.org/w/api.php?action=query&format=xml&list=categorymembers&cmlimit=5000&cmtitle=";

    private List<String> categories = new ArrayList<>();

    private DocumentBuilderFactory factory;

    private Logger log = LoggerFactory.getLogger(getClass());

    private Set<String> pages = new HashSet<>();

    private String path;

    private XPath xpath;

    public WikiPageExtractor(final String outputPath, final String categories) {
        for (String l : categories.split(",")) {
            this.categories.add(l);
        }
        this.factory = DocumentBuilderFactory.newInstance();
        this.xpath = XPathFactory.newInstance().newXPath();
        this.path = outputPath;
        File directory = new File(this.path);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    @Override
    public void extract() {
        this.log.info(" - start - page extraction");
        this.log.info("path: " + this.path);
        this.log.info("categories: " + this.categories);
        for (String cat : this.categories) {
            extract(cat.trim());
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("pages.obj"))) {
            oos.writeObject(this.pages);
        } catch (IOException e) {
            this.log.error(e.getMessage(), e);
        }
        this.log.info(" - end - page extraction");
    }

    public void extract(final String cat) {
        try {
            String query = BASE_URL + "&cmtitle=" + URLEncoder.encode(cat, "UTF-8");
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            FileWriter fw = new FileWriter(this.path + "/" + date + "-out.txt", true);
            boolean more = true;
            String cmcontinue = "";
            while (more) {
                String xmlContent = getContent(query + "&cmcontinue=" + URLEncoder.encode(cmcontinue, "UTF-8"));
                Document doc;
                NodeList cmNodes = null;
                try {
                    doc = this.factory.newDocumentBuilder().parse(new ByteArrayInputStream(xmlContent.getBytes()));
                    NodeList continueNode = (NodeList) this.xpath.evaluate("/api/continue", doc,
                            XPathConstants.NODESET);
                    if (continueNode.getLength() == 0) {
                        more = false;
                    } else {
                        cmcontinue = (String) this.xpath.evaluate("/api/continue/@cmcontinue", doc,
                                XPathConstants.STRING);
                    }
                    cmNodes = (NodeList) this.xpath.evaluate("/api/query/categorymembers/cm", doc,
                            XPathConstants.NODESET);
                    for (int n = 0; n < cmNodes.getLength(); n++) {
                        Element el = (Element) cmNodes.item(n);
                        StringBuilder sb = new StringBuilder();
                        String ns = el.getAttribute("ns");
                        String title = el.getAttribute("title");
                        sb.append(cat);
                        // sb.append(TAB).append(el.getAttribute("pageid"));
                        sb.append(TAB).append(ns);
                        sb.append(TAB).append(title);
                        sb.append(RETURN);
                        writeLine(fw, cat, ns, title);
                        if ("1".equals(ns)) {
                            // if need pageid, need another API call, 500 titles at a time for bots:
                            // /w/api.php?action=query&format=json&prop=pageprops&titles=Fungus%7CGenetics
                            writeLine(fw, cat, "0", title.replaceFirst("^Talk:", ""));
                        }
                        fw.write(sb.toString());
                    }
                } catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException e) {
                    this.log.error("failed to fetch data", e);
                }
            }
            fw.close();
        } catch (IOException e) {
            this.log.error(e.getMessage(), e);
            throw new WikiExtractException(e);
        }
    }

    private void writeLine(final FileWriter fw, final String cat, final String ns, final String title)
            throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(cat);
        // sb.append(TAB).append(el.getAttribute("pageid"));
        sb.append(TAB).append(ns);
        sb.append(TAB).append(title);
        sb.append(RETURN);
        fw.write(sb.toString());
        this.pages.add(ns + ":::" + title);
    }
}
