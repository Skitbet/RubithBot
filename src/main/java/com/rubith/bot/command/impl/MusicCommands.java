package com.rubith.bot.command.impl;

import com.rubith.bot.command.annotation.CommandInfo;
import com.rubith.bot.command.annotation.Option;
import com.rubith.bot.lavaplayer.PlayerManager;
import com.rubith.bot.lavaplayer.TrackScheduler;
import com.rubith.bot.util.MessageUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;

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
        event.replyEmbeds(MessageUtils.createEmbed(event.getUser(), "The player volume has been updated to " + volume, Color.GREEN)).queue();
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
        event.replyEmbeds(MessageUtils.createEmbed(event.getUser(), "Repeat is now " + (getTrack(event.getGuild()).isRepeat() ? "enabled." : "disabled."), Color.GREEN)).queue();

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

        event.replyEmbeds(MessageUtils.createEmbed(event.getUser(), "Please give me a second to load!", Color.BLUE)).queue();

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

        event.replyEmbeds(MessageUtils.createEmbed(event.getUser(), "I have skipped to the next track!", Color.GREEN)).queue();

        getTrack(event.getGuild()).getPlayer().stopTrack();
    }


    public static boolean memberInChannel(Member member) {
        GuildVoiceState voiceState = member.getVoiceState();
        if(!voiceState.inAudioChannel()) {
            return false;
        }
        return true;
    }

    public static TrackScheduler getTrack(Guild guild) {
        PlayerManager playerManager = PlayerManager.get();
        return playerManager.getGuildMusicManager(guild).getTrackScheduler();
    }

}
