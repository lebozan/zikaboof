package org.bodzan.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PageInfoJson {

    public int totalResults;
    public int resultsPerPage;
}
