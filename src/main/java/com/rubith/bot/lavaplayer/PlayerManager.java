package com.rubith.bot.lavaplayer;

import com.rubith.bot.util.MessageUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.awt.*;
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
                hook.editOriginalEmbeds(MessageUtils.createEmbed(hook.getInteraction().getUser(), "New Audio Queued!", "You have queued: " + track.getInfo().title, Color.GREEN)).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                guildMusicManager.getTrackScheduler().queue(playlist.getTracks().get(0));
                hook.editOriginalEmbeds(MessageUtils.createEmbed(hook.getInteraction().getUser(), "New Audio Queued!", "You have queued: " + playlist.getTracks().get(0).getInfo().title, Color.GREEN)).queue();
            }

            @Override
            public void noMatches() {
                hook.editOriginalEmbeds(MessageUtils.createEmbed(hook.getInteraction().getUser(), "Uh oh! Seems like I couldn't find what you wanted!", Color.RED)).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                hook.editOriginalEmbeds(MessageUtils.createEmbed(hook.getInteraction().getUser(), "Uh oh! Seems like I ran into a error.", Color.RED)).queue();
            }
        });
    }


}