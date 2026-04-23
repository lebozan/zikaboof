package org.bodzan.bot;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.time.Instant;

public class SongLinkWithEmbed {

    private String songLink;

    private EmbedCreateSpec embed;


    public SongLinkWithEmbed() {
        this.embed = EmbedCreateSpec.builder()
                .color(Color.GREEN)
                .title("Свирам песму")
                .author("Жика музичар", "", "https://i.kym-cdn.com/photos/images/newsfeed/000/281/837/986.gif")
                .description("")
                .timestamp(Instant.now())
                .build();
        this.songLink = "";
    }

    public String getSongLink() {
        return songLink;
    }

    public void setSongLink(String songLink) {
        this.songLink = songLink;
    }

    public EmbedCreateSpec getEmbed() {
        return embed;
    }

    public void setEmbed(EmbedCreateSpec embed) {
        this.embed = embed;
    }
}
