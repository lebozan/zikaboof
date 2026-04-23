package org.bodzan.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemJsonPlainId {
    public String kind;
    public String etag;
    public String id;
    public SnippetJson snippet;
}
