package org.bodzan.bot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class AudioTrackScheduler extends AudioEventAdapter implements AudioLoadResultHandler {
    private static final Logger log = Logger.getLogger(AudioTrackScheduler.class.getName());
    private final List<AudioTrack> queue;
    private final AudioPlayer player;

    public AudioTrackScheduler(AudioPlayer player) {
        // The queue may be modifed by different threads so guarantee memory safety
        // This does not, however, remove several race conditions currently present
        queue = Collections.synchronizedList(new LinkedList<>());
        this.player = player;
    }

    public List<AudioTrack> getQueue() {
        return queue;
    }

    public boolean play(AudioTrack track, boolean force) {
        boolean playing = player.startTrack(track, !force);

        if (!playing) {
            queue.add(track);
        }

        return playing;
    }

    public void skip() {
        if (!queue.isEmpty()) {
            play(queue.remove(0), true);
        } else {
            player.stopTrack();
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Advance the player if the track completed naturally (FINISHED) or if the track cannot play (LOAD_FAILED)
        if (endReason.mayStartNext) {
            skip();
        }
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        play(track, false);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {

    }

    @Override
    public void noMatches() {
        log.warning("LavaPlayer: no matches found for the provided identifier");
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        log.severe("LavaPlayer: load failed — " + exception.getMessage());
    }
}
