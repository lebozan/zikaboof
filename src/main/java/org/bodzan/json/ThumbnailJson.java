package org.bodzan.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ThumbnailJson {
    public String url;
    public int width;
    public int height;
}
