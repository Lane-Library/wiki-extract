package edu.stanford.lane.extraction;

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

/**
 * @author ryanmax
 */
public abstract class AbstractExtractor {

    protected static final RequestConfig HTTP_CONFIG = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES)
            .build();

    protected static final HttpClient httpClient = HttpClients.createDefault();

    private Logger log = LoggerFactory.getLogger(getClass());

    protected String getContent(final String url) {
        String content = null;
        HttpResponse res = null;
        HttpGet method = new HttpGet(url);
        method.setConfig(HTTP_CONFIG);
        try {
            res = AbstractExtractor.httpClient.execute(method);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                content = EntityUtils.toString(res.getEntity());
            }
        } catch (Exception e) {
            this.log.error(e.getMessage(), e);
            method.abort();
        } finally {
            method.releaseConnection();
        }
        return content;
    }
}
