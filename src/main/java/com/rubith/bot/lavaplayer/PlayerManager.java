package com.rubith.bot.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.awt.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PlayerManager {

    private static PlayerManager INSTANCE;
    private Map<Long, GuildMusicManager> guildMusicManagers = new HashMap<>();
    private AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();

    private PlayerManager() {
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
    }

    public static PlayerManager get() {
        if(INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

    public GuildMusicManager getGuildMusicManager(Guild guild) {
        return guildMusicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            GuildMusicManager musicManager = new GuildMusicManager(audioPlayerManager, guild);

            guild.getAudioManager().setSendingHandler(musicManager.getAudioForwarder());

            return musicManager;
        });
    }

    public void play(Guild guild, String trackURL, InteractionHook hook) {
        GuildMusicManager guildMusicManager = getGuildMusicManager(guild);
        audioPlayerManager.loadItemOrdered(guildMusicManager, trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                guildMusicManager.getTrackScheduler().queue(track);
                hook.editOriginalEmbeds(
                        new EmbedBuilder()
                                .setTitle("New Track Queued!")
                                .setDescription("A new track has been added to the queue!")
                                .setColor(0x3447003)
                                .addField("Track Title", track.getInfo().title, true)
                                .addField("Uploaded By", track.getInfo().author, true)
                                .addField("Duration", formattedDuration(track.getInfo().length), true)
                                .addField("Queue Position", String.valueOf(guildMusicManager.getTrackScheduler().getQueue().size()), true)
                                .setFooter(hook.getInteraction().getUser().getName(), hook.getInteraction().getUser().getEffectiveAvatarUrl())
                                .setTimestamp(new Date().toInstant())
                                .build()
                ).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack track = playlist.getTracks().get(0);
                guildMusicManager.getTrackScheduler().queue(track);

                hook.editOriginalEmbeds(
                        new EmbedBuilder()
                                .setTitle("New Track Queued!")
                                .setDescription("A new track has been added to the queue!")
                                .setColor(0x3447003)
                                .addField("Track Title", track.getInfo().title, true)
                                .addField("Uploaded By", track.getInfo().author, true)
                                .addField("Duration", formattedDuration(track.getInfo().length), true)
                                .addField("Queue Position", String.valueOf(guildMusicManager.getTrackScheduler().getQueue().size()), true)
                                .setFooter(hook.getInteraction().getUser().getName(), hook.getInteraction().getUser().getEffectiveAvatarUrl())
                                .setTimestamp(new Date().toInstant())
                                .build()
                ).queue();            }

            @Override
            public void noMatches() {
                hook.editOriginalEmbeds(
                        new EmbedBuilder()
                                .setTitle("Uh Oh!")
                                .setDescription("I could not find a what your looking for!")
                                .setColor(Color.RED)
                                .setFooter(hook.getInteraction().getUser().getName(), hook.getInteraction().getUser().getEffectiveAvatarUrl())
                                .setTimestamp(new Date().toInstant())
                                .build()
                ).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                hook.editOriginalEmbeds(
                        new EmbedBuilder()
                                .setTitle("Uh Oh!")
                                .setDescription("I don't know what happened!")
                                .setColor(Color.RED)
                                .setFooter(hook.getInteraction().getUser().getName(), hook.getInteraction().getUser().getEffectiveAvatarUrl())
                                .setTimestamp(new Date().toInstant())
                                .build()
                ).queue();
            }
        });
    }

    private static String formattedDuration(long milliseconds) {
        long totalSeconds = milliseconds / 1000; // Convert milliseconds to seconds

        long hours = totalSeconds / 3600; // Divide by 3600 to get the number of hours
        long minutes = (totalSeconds % 3600) / 60; // Divide the remaining seconds by 60 to get the number of minutes
        long remainingSeconds = totalSeconds % 60; // Get the remaining seconds

        String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
        return formattedTime;
    }


}