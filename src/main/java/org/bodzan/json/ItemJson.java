package org.bodzan.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemJson {
    public String kind;
    public String etag;
    public IdJson id;
    public SnippetJson snippet;
}
