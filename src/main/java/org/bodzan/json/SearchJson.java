package org.bodzan.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchJson {

    public String kind;
    public String etag;
    public String nextPageToken;
    public String regionCode;
    public PageInfoJson pageInfo;

    public List<ItemJson> items;
}
