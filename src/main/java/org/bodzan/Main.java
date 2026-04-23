package org.bodzan;

import io.github.cdimascio.dotenv.Dotenv;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.Color;
import discord4j.voice.AudioProvider;
import discord4j.voice.VoiceConnection;
import org.bodzan.bot.*;
import org.bodzan.json.SearchJson;
import org.bodzan.json.SearchJsonPlainId;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

public class Main {

    private static SongLinkWithEmbed searchForVideoOrGetLink(String keyword, String ytApiKey)  {
        SongLinkWithEmbed songLinkWithEmbed = new SongLinkWithEmbed();
        String ytLink = "https://www.youtube.com/watch?v=";
//        StringBuilder keyword = new StringBuilder();

//        keyword.append(keywords.get(1));

        if (keyword.contains("://")) {
            songLinkWithEmbed.setSongLink(keyword);
            String videoId;
            if (keyword.contains("watch")) {
                String[] split = keyword.split("v=");
                videoId = split[1];
            } else if (keyword.contains("tu.be")) {
                String[] split = keyword.split("/");
                videoId = split[3];
            } else {
                return songLinkWithEmbed;
            }
            try {
                URL url = new URL(String.format("https://youtube.googleapis.com/youtube/v3/videos?part=snippet&id=%s&key=%s", videoId, ytApiKey));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                JsonFactory jfactory = new JsonFactory();
                JsonParser jParser = jfactory.createParser(content.toString());

                ObjectMapper mapper = new ObjectMapper();
                SearchJsonPlainId searchJson = mapper.readValue(jParser, SearchJsonPlainId.class);
                songLinkWithEmbed.setEmbed(songLinkWithEmbed.getEmbed().withDescription("Свирам: " + searchJson.items.get(0).snippet.title));
                jParser.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return songLinkWithEmbed;
        }
        keyword = keyword.replaceAll(" ", "+");

        String queryFormatted = String.format("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&q=%s&type=video&key=%s", keyword, ytApiKey);
        try {
            URL url = new URL(queryFormatted);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            JsonFactory jfactory = new JsonFactory();
            JsonParser jParser = jfactory.createParser(content.toString());

            ObjectMapper mapper = new ObjectMapper();
            SearchJson searchJson = mapper.readValue(jParser, SearchJson.class);
            String videoId = searchJson.items.get(0).id.videoId;
            songLinkWithEmbed.setEmbed(songLinkWithEmbed.getEmbed().withDescription("Свирам: " + searchJson.items.get(0).snippet.title));
            jParser.close();
            ytLink += videoId;
            songLinkWithEmbed.setSongLink(ytLink);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return songLinkWithEmbed;
    }


    private static void addCommandsToGuild(long guildId, GatewayDiscordClient client) {
        long applicationId = client.getRestClient().getApplicationId().block();

        // Build our command's definition
        ApplicationCommandRequest joinCmdRequest = ApplicationCommandRequest.builder()
                .name("join")
                .description("Жика улази у канал")
                .build();

        client.getRestClient().getApplicationService()
                .createGuildApplicationCommand(applicationId, guildId, joinCmdRequest)
                .subscribe();

        ApplicationCommandRequest playCmdRequest = ApplicationCommandRequest.builder()
                .name("play")
                .description("Жика свира")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("keyword")
                        .description("ime pesme ili link")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build()
                ).build();

        client.getRestClient().getApplicationService()
                .createGuildApplicationCommand(applicationId, guildId, playCmdRequest)
                .subscribe();

        ApplicationCommandRequest skipCmdRequest = ApplicationCommandRequest.builder()
                .name("skip")
                .description("Жика прескаче песму")
                .build();

        client.getRestClient().getApplicationService()
                .createGuildApplicationCommand(applicationId, guildId, skipCmdRequest)
                .subscribe();

        ApplicationCommandRequest leaveCmdRequest = ApplicationCommandRequest.builder()
                .name("leave")
                .description("Жика напушта канал")
                .build();

        client.getRestClient().getApplicationService()
                .createGuildApplicationCommand(applicationId, guildId, leaveCmdRequest)
                .subscribe();
    }

    public static void deleteCommandsFromGuild(long guildId, GatewayDiscordClient client) {
        long applicationId = client.getRestClient().getApplicationId().block();

        // Get the commands from discord as a Map
        Map<String, ApplicationCommandData> discordCommands = client.getRestClient()
                .getApplicationService()
                .getGuildApplicationCommands(applicationId, guildId)
                .collectMap(ApplicationCommandData::name)
                .block();

        // Get the ID of our greet command
        long playCommandId = Long.parseLong(String.valueOf(discordCommands.get("play").id()));
        long joinCommandId = Long.parseLong(String.valueOf(discordCommands.get("join").id()));
        long leaveCommandId = Long.parseLong(String.valueOf(discordCommands.get("leave").id()));
        long skipCommandId = Long.parseLong(String.valueOf(discordCommands.get("skip").id()));

        // Delete it
        client.getRestClient().getApplicationService()
                .deleteGuildApplicationCommand(applicationId, guildId, playCommandId)
                .subscribe();
        client.getRestClient().getApplicationService()
                .deleteGuildApplicationCommand(applicationId, guildId, joinCommandId)
                .subscribe();
        client.getRestClient().getApplicationService()
                .deleteGuildApplicationCommand(applicationId, guildId, leaveCommandId)
                .subscribe();
        client.getRestClient().getApplicationService()
                .deleteGuildApplicationCommand(applicationId, guildId, skipCommandId)
                .subscribe();
    }

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String token = dotenv.get("DISCORD_TOKEN");
        String ytApiKey = dotenv.get("YOUTUBE_API_KEY");
        Long testguildId = 997989667045658735L;
        long nasGuildId = 997894824004956320L;
        long domServerGuildId = 705920358552829952L;
        List<Long> guildIds = List.of(nasGuildId, domServerGuildId);

        Logger log = Logger.getLogger(Main.class.getName());


        // Creates AudioPlayer instances and translates URLs to AudioTrack instances
        final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

        // This is an optimization strategy that Discord4J can utilize.
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);

        // Allow playerManager to parse remote sources like YouTube links
        AudioSourceManagers.registerRemoteSources(playerManager);

        // Create an AudioPlayer so Discord4J can receive audio data
        final AudioPlayer player = playerManager.createPlayer();

        // We will be creating LavaPlayerAudioProvider in the next step
        AudioProvider provider = new LavaPlayerAudioProvider(player);

        final AudioTrackScheduler scheduler = new AudioTrackScheduler(player);

        player.addListener(scheduler);

        EmbedCreateSpec joinEmbed = EmbedCreateSpec.builder()
                .color(Color.GREEN)
                .title("Упадам у воис")
                .author("Жика музичар", "", "https://i.kym-cdn.com/photos/images/newsfeed/000/281/837/986.gif")
                .description("")
                .image("https://media.tenor.com/gNnt28OTWCUAAAAd/yakuza0-kiryu.gif")
                .timestamp(Instant.now())
                .build();

        EmbedCreateSpec skipEmbed = EmbedCreateSpec.builder()
                .color(Color.GREEN)
                .title("Прескачем песму")
                .author("Жика музичар", "", "https://i.kym-cdn.com/photos/images/newsfeed/000/281/837/986.gif")
                .description("Даље иде даље иде")
                .timestamp(Instant.now())
                .build();

        EmbedCreateSpec leaveEmbed = EmbedCreateSpec.builder()
                .color(Color.GREEN)
                .title("Отишо сам")
                .author("Жика музичар", "", "https://i.kym-cdn.com/photos/images/newsfeed/000/281/837/986.gif")
                .description("Излазим из канала")
                .image("https://cdn.discordapp.com/attachments/997989667754491996/1137845060646473808/moj_ti_usput_5.gif")
                .timestamp(Instant.now())
                .build();


        final GatewayDiscordClient client = DiscordClientBuilder.create(token).build()
                .login().block();

//        guildIds.forEach(guildId -> addCommandsToGuild(guildId, client));

        client.on(ApplicationCommandInteractionEvent.class, event -> {
            if (event.getCommandName().equals("join")) {
                Mono<Void> joinVoice = event.getInteraction().getMember().get().getVoiceState()
                        .flatMap(VoiceState::getChannel)
                        .flatMap(channel -> channel.join(spec -> spec.setProvider(GuildAudioManager.of(channel.getGuildId()).getProvider())))
                        .then();
                return joinVoice.and(event.reply().withEmbeds(joinEmbed));
            } else if (event.getCommandName().equals("play")) {
                SongLinkWithEmbed linkWithEmbed = event.getInteraction().getCommandInteraction()
                        .flatMap(commandInteraction -> commandInteraction.getOption("keyword"))
                        .flatMap(ApplicationCommandInteractionOption::getValue)
                        .map(applicationCommandInteractionOptionValue -> searchForVideoOrGetLink(applicationCommandInteractionOptionValue.asString(), ytApiKey))
                        .orElseThrow();
                Snowflake currentGID = event.getInteraction().getGuildId().orElseThrow();
                GuildAudioManager.of(currentGID).PLAYER_MANAGER.loadItem(linkWithEmbed.getSongLink(), GuildAudioManager.of(currentGID).getScheduler());
                return event.reply().withEmbeds(linkWithEmbed.getEmbed());
            } else if (event.getCommandName().equals("skip")) {
                GuildAudioManager.of(event.getInteraction().getGuildId().orElseThrow()).getScheduler().skip();
                return event.reply().withEmbeds(skipEmbed);
            } else if (event.getCommandName().equals("leave")) {
                return Mono.justOrEmpty(event.getInteraction().getMember().orElseThrow())
                        .flatMap(Member::getVoiceState)
                        .flatMap(voiceState -> client.getVoiceConnectionRegistry().getVoiceConnection(voiceState.getGuildId())
                                .doOnSuccess(vc -> {
                                    if (vc == null) {
                                        log.info("No voice connection to leave!");
                                    }
                                }))
                        .flatMap(VoiceConnection::disconnect).and(event.reply().withEmbeds(leaveEmbed));
            }
            return Mono.empty();
        }).subscribe();


        client.onDisconnect().block();
    }
}