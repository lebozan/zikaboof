package org.bodzan.json;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ThumbnailWrapJson {

    public ThumbnailJson difolt;


    @JsonSetter("default")
    public void setDifolt(ThumbnailJson difolt) {
        this.difolt = difolt;
    }

    @JsonGetter("default")
    public ThumbnailJson getDifolt() {
        return difolt;
    }

    public ThumbnailJson medium;
    public ThumbnailJson high;
    public ThumbnailJson standard;
    public ThumbnailJson maxres;

}
