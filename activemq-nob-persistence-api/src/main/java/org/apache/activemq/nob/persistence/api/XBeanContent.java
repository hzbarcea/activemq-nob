package org.apache.activemq.nob.persistence.api;

/**
 * Store details of XBean content for a broker configuration.
 *
 * Created by art on 2/20/15.
 */
public class XBeanContent {
    /**
     * The XML content itself.
     */
    private final String  content;

    /**
     * The time the content was last modified.
     */
    private final long    lastModified;

    public XBeanContent(String content, long lastModified) {
        this.content = content;
        this.lastModified = lastModified;
    }

    public String getContent() {
        return content;
    }

    public long getLastModified() {
        return lastModified;
    }
}
