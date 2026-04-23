package org.bodzan.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IdJson {
    public String kind;
    public String videoId;
}
