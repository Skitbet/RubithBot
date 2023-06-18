package com.rubith.bot.command.impl;

import com.rubith.bot.command.annotation.CommandInfo;
import com.rubith.bot.command.annotation.Option;
import com.rubith.bot.lavaplayer.PlayerManager;
import com.rubith.bot.lavaplayer.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

public class MusicCommands {

    @CommandInfo(name = "volume", description = "Change the volume")
    public static void onVolumeCommand(SlashCommandInteractionEvent event, @Option(name = "volume", description = "The volume you want.", type = OptionType.INTEGER) int volume) {

        Member member = event.getMember();
        GuildVoiceState voiceState = member.getVoiceState();

        if(!voiceState.inAudioChannel()) {
            event.reply("You are not in a channel!").queue();
            return;
        }

        GuildVoiceState selfVoiceState = event.getGuild().getSelfMember().getVoiceState();
        if (!selfVoiceState.inAudioChannel()) {
            event.reply("You must queue something first :/").queue();
            return;
        } else {
            if(selfVoiceState.getChannel() != voiceState.getChannel()) {
                event.reply("You need to be in the same channel as me").queue();
                return;
            }
        }

        getTrack(event.getGuild()).getPlayer().setVolume(volume);
        event.replyEmbeds(new EmbedBuilder()
                .setTitle("Volume has been changed!")
                .setDescription("The current volume is now " + volume)
                .setColor(0x3447003)
                .setFooter(event.getUser().getName(), event.getUser().getEffectiveAvatarUrl())
                .setTimestamp(new Date().toInstant())
                .build()).queue();
    }

    @CommandInfo(name = "repeat", description = "Toggles the repeat status")
    public static void onRepeatCommand(SlashCommandInteractionEvent event) {

        Member member = event.getMember();
        GuildVoiceState voiceState = member.getVoiceState();

        if(!voiceState.inAudioChannel()) {
            event.reply("You are not in a channel!").queue();
            return;
        }

        GuildVoiceState selfVoiceState = event.getGuild().getSelfMember().getVoiceState();
        if (!selfVoiceState.inAudioChannel()) {
            event.reply("You must queue something first :/").queue();
            return;
        } else {
            if(selfVoiceState.getChannel() != voiceState.getChannel()) {
                event.reply("You need to be in the same channel as me").queue();
                return;
            }
        }

        getTrack(event.getGuild()).setRepeat(!getTrack(event.getGuild()).isRepeat());
        event.replyEmbeds( new EmbedBuilder()
                .setTitle("Repeat has been toggled!")
                .setDescription("The song will " + (getTrack(event.getGuild()).isRepeat() ? "now repeat!" : "no longer repeat!"))
                .setColor(0x3447003)
                .setFooter(event.getUser().getName(), event.getUser().getEffectiveAvatarUrl())
                .setTimestamp(new Date().toInstant())
                .build()).queue();

    }

    @CommandInfo(name = "play", description = "Add a track to the queue")
    public static void onPlayCommand(SlashCommandInteractionEvent event,
                                     @Option(name = "search", description = "What to search or a url") String search) {
        Member member = event.getMember();
        GuildVoiceState voiceState = member.getVoiceState();

        if(!voiceState.inAudioChannel()) {
            event.reply("You are not in a channel!").queue();
            return;
        }

        GuildVoiceState selfVoiceState = event.getGuild().getSelfMember().getVoiceState();
        if (!selfVoiceState.inAudioChannel()) {
            event.getGuild().getAudioManager().openAudioConnection(voiceState.getChannel());
        } else {
            if(selfVoiceState.getChannel() != voiceState.getChannel()) {
                event.reply("You need to be in the same channel as me").queue();
                return;
            }
        }

        event.deferReply().queue();

        String name = search;
        try {
            new URI(name);
        } catch (URISyntaxException e) {
            name = "ytsearch:" + name;
        }

        PlayerManager playerManager = PlayerManager.get();
        playerManager.play(event.getGuild(), name, event.getHook());
    }

    @CommandInfo(name = "skip", description = "Skip the current track.")
    public static void onSkipCommand(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        GuildVoiceState voiceState = member.getVoiceState();

        if(!voiceState.inAudioChannel()) {
            event.reply("You are not in a channel!").queue();
            return;
        }

        GuildVoiceState selfVoiceState = event.getGuild().getSelfMember().getVoiceState();
        if (!selfVoiceState.inAudioChannel()) {
            event.reply("You must queue something first :/").queue();
            return;
        } else {
            if(selfVoiceState.getChannel() != voiceState.getChannel()) {
                event.reply("You need to be in the same channel as me").queue();
                return;
            }
        }
        getTrack(event.getGuild()).getPlayer().stopTrack();
        AudioTrack track = getTrack(event.getGuild()).getPlayer().getPlayingTrack();

        event.replyEmbeds( new EmbedBuilder()
                .setTitle("The track has been skipped!")
                .setDescription("Now playing the next track in queue if one is there.")
                .addField("Track Title", track.getInfo().title, true)
                .addField("Uploaded By", track.getInfo().author, true)
                .addField("Duration", formattedDuration(track.getInfo().length), true)
                .setColor(0x3447003)
                .setFooter(event.getUser().getName(), event.getUser().getEffectiveAvatarUrl())
                .setTimestamp(new Date().toInstant())
                .build()).queue();


    }


    private static boolean memberInChannel(Member member) {
        GuildVoiceState voiceState = member.getVoiceState();
        if(!voiceState.inAudioChannel()) {
            return false;
        }
        return true;
    }

    private static TrackScheduler getTrack(Guild guild) {
        PlayerManager playerManager = PlayerManager.get();
        return playerManager.getGuildMusicManager(guild).getTrackScheduler();
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
