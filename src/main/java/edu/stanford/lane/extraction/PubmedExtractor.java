package edu.stanford.lane.extraction;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author ryanmax
 */
public class PubmedExtractor extends AbstractExtractor implements Extractor {

    // public class PubmedArticle {
    //
    // private String pmid;
    //
    // private List<String> pubTypes;
    //
    // private String title;
    //
    // public PubmedArticle(final String pmid, final String title, final List<String> pubTypes) {
    // this.pmid = pmid;
    // this.title = title;
    // this.pubTypes = pubTypes;
    // }
    //
    // public void addPubType(final String type) {
    // this.pubTypes.add(type);
    // }
    //
    // public String getPmid() {
    // return this.pmid;
    // }
    //
    // public List<String> getPubTypes() {
    // return this.pubTypes;
    // }
    //
    // public String getTitle() {
    // return this.title;
    // }
    // }
    //
    private static final String EFETCH_BASE_URL = "https://ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&retmode=xml&id=";

    private static final String ESEARCH_BASE_URL = "https://ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&retmax=1&retmode=json&term=";

    protected DocumentBuilderFactory factory;

    protected XPath xpath;

    private String doiFile;

    private Logger log = LoggerFactory.getLogger(getClass());

    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Assumes doiFile will contain a list of DOIs; for each DOI found, search PubMed and extract publication
     * information
     *
     * @param doiFile
     */
    public PubmedExtractor(final String doiFile) {
        this.doiFile = doiFile;
        this.factory = DocumentBuilderFactory.newInstance();
        this.xpath = XPathFactory.newInstance().newXPath();
    }

    @Override
    public void extract() {
        File in = new File(this.doiFile);
        if (in.exists()) {
            extract(in);
        }
    }

    protected String doiToPmid(final String doi) {
        String pmid = "";
        String json = getContent(ESEARCH_BASE_URL + "\"" + doi + "\"[aid]+OR+" + "\"" + doi + "\"[lid]");
        if (null != json && !json.isEmpty()) {
            Map<String, Object> data = null;
            try {
                data = this.mapper.readValue(json, Map.class);
            } catch (IOException e) {
                this.log.error("error parsing json for doi: " + doi, e);
            }
            HashMap<String, Object> map1 = (HashMap<String, Object>) data.get("esearchresult");
            int count = 0;
            try {
                count = Integer.parseInt((String) map1.get("count"));
            } catch (NumberFormatException e) {
                this.log.error("can't parse count from json: " + json, e);
            }
            if (count == 1) {
                pmid = ((List<String>) map1.get("idlist")).get(0);
            } else if (count > 1) {
                this.log.info("more than one pmid for doi: " + doi);
            }
        }
        return pmid;
    }

    protected List<String> pmidToPubTypes(final String pmid) {
        List<String> types = new ArrayList<>();
        String xml = getContent(EFETCH_BASE_URL + pmid);
        if (null == xml) {
            return types;
        }
        try {
            Document doc = this.factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));
            NodeList nodes = (NodeList) this.xpath.evaluate("//Article/PublicationTypeList/PublicationType", doc,
                    XPathConstants.NODESET);
            for (int n = 0; n < nodes.getLength(); n++) {
                Node node = nodes.item(n);
                types.add(node.getTextContent().trim());
            }
        } catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException e) {
            this.log.error("error parsing xml for pmid: " + pmid, e);
            this.log.error("xml: " + xml);
        }
        return types;
    }

    private void extract(final File input) {
        try (BufferedReader br = new BufferedReader(new FileReader(input));
                FileWriter fw = new FileWriter(this.doiFile + "-pm-types-out.txt", true);) {
            String doi;
            while ((doi = br.readLine()) != null) {
                String pmid = doiToPmid(doi);
                if (!pmid.isEmpty()) {
                    for (String type : pmidToPubTypes(pmid)) {
                        fw.write(doi);
                        fw.write("\t");
                        fw.write(pmid);
                        fw.write("\t");
                        fw.write(type);
                        fw.write("\n");
                    }
                }
            }
        } catch (IOException e) {
            this.log.error("error extracting from file: " + input, e);
        }
    }
}
