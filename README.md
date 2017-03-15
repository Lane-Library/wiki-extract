# Extract external links from Wikipedia

## Prerequisites
* Java 8
* [Apache Maven](http://maven.apache.org/)

## Build
```
$ cd wiki-extract
$ mvn install
```

## Create runtime properties
* review [default.properties](default.properties) and save your own copy as wiki-extractor.properties

## Run
* To extract data, run one of:
  * **extract-pages-and-links.xml**: extract a list of all [WikiProject Medicine](https://en.wikipedia.org/wiki/Wikipedia:WikiProject_Medicine) pages and then extract all doi.org external links from all Wikipedia pages
  * **extract-pubmed.xml**: from a list of DOIs, extract article information from PubMed
  * **extract-stats.xml**: from a list of Wikipedia pages, extract usage data for each
* Example:
```
$ cd wiki-extract
$ java -classpath target/wiki-extract.jar edu.stanford.lane.Main extract-pages-and-links.xml
```
* Output from extract-pages-and-links.xml will be saved in YYYY-MM-DD/en/out.txt files
     * ================================================================================
     * legend for YYYY-MM-DD/en/out.txt files:
     * ================================================================================
     * language (all en)
     * pageid (https://en.wikipedia.org/?curid=XXXX to fetch page)
     * namespace (https://en.wikipedia.org/wiki/Wikipedia:Namespace)
     * isProjectMedicinePage
     * page_title
     * link_to_doi.org

* Summarizer summarizes data from YYYY-MM-DD/en/out.txt files, parsing all DOIs and even expanding "shortened" DOIs to their longer form. Output is:
     * ================================================================================
     * legend for summary.txt files:
     * ================================================================================
     * DOI
     * category of unique DOI, where category is one of:
     * - CAT_1_ONLY_PROJECT_MED: only found on project med pages over the 31 days of August
     * - CAT_2_ONLY_NON_PROJECT_MED: only found on non-project med pages over the 31 days of August
     * - CAT_3_BOTH_PROJECT_MED_AND_NON_PROJECT_MED: found on both med and non-med pages over the 31 days of August

* To run Summarizer:
```
$ java -classpath target/wiki-extract.jar edu.stanford.lane.report.Summarizer /path/to/YYYY-MM-D1/en/out.txt /path/to/YYYY-MM-D2/en/out.txt
```

### References & Links

* [Wikipedia API example of all doi.org external links](https://en.wikipedia.org/wiki/Special:ApiSandbox#action=query&list=exturlusage&format=xml&euprop=ids|title|url&euquery=*.doi.org)

* [Wikipedia Pageviews API](https://wikitech.wikimedia.org/wiki/Analytics/PageviewAPI)

* [Definition of a Wikipedia article; includes definitions of wiki namespaces](https://en.wikipedia.org/wiki/Wikipedia:What_is_an_article%3F)

* [list of Wikipedias](https://en.wikipedia.org/wiki/List_of_Wikipedias)

* [WikiProject Medicine pages by category](https://en.wikipedia.org/wiki/Category:Medicine_articles_by_quality)
