package org.bodzan.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SnippetJson {
    public String publishedAt;
    public String channelId;
    public String title;
    public String description;
    public String channelTitle;
    public String liveBroadcastContent;
    public String publishTime;
    public ThumbnailWrapJson thumbnails;
    public List<String> tags;
    public String categoryId;
    public Object localized;
    public String defaultAudioLanguage;
}
