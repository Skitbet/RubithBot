package com.rubith.bot;

import com.rubith.bot.command.CommandHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class RubithBot {

    public static RubithBot instance;

    public JDA jda;

    public boolean devMode;

    public CommandHandler commandHandler;

    public RubithBot(String token, boolean devMode) {
        this.devMode = devMode;


        JDABuilder jdaBuilder = JDABuilder.create(token,
                GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.MESSAGE_CONTENT
        );

        jdaBuilder.setActivity(Activity.watching("You"));
        jdaBuilder.setStatus(OnlineStatus.DO_NOT_DISTURB);

        this.jda = jdaBuilder.build();
        this.commandHandler = new CommandHandler(this.jda);

        this.jda.addEventListener(commandHandler);
    }


    public static void main(String[] args) {
        new RubithBot(args[0], Boolean.parseBoolean(args[1]));
    }

    public static RubithBot getInstance() {
        return instance;
    }

    public JDA getJda() {
        return jda;
    }

    public boolean isDevMode() {
        return devMode;
    }
}