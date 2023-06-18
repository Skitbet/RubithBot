package com.rubith.bot.command.impl;

import com.rubith.bot.command.annotation.CommandInfo;
import com.rubith.bot.command.annotation.Option;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class TestCommands {

    @CommandInfo(name = "ping", description = "Ping command")
    public static void handlePing(SlashCommandInteractionEvent event) {
        event.reply("Pong!").queue();
    }

    @CommandInfo(name = "say", description = "Say command")
    public static void handleSay(SlashCommandInteractionEvent event,
                                 @Option(name = "message", description = "The message to say") String message,
                                 @Option(name = "private", description = "Should be private?", type = OptionType.BOOLEAN, required = false) boolean isPrivate) {

        event.reply(message).setEphemeral(isPrivate).queue();
    }

}
