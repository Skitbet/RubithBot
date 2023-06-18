package com.rubith.bot.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.util.Date;

public class MessageUtils {

    public static MessageEmbed createEmbed(User user, String title, String description, Color color) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(color)
                .setFooter(user.getName(), user.getEffectiveAvatarUrl())
                .setTimestamp(new Date().toInstant())
                .build();
    }

    public static MessageEmbed createEmbed(User user, String title, Color color) {
        return new EmbedBuilder()
                .setTitle(title)
                .setColor(color)
                .setFooter(user.getName(), user.getEffectiveAvatarUrl())
                .setTimestamp(new Date().toInstant())
                .build();
    }

}
