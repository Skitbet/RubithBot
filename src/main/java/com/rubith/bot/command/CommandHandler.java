package com.rubith.bot.command;

import com.rubith.bot.command.annotation.CommandInfo;
import com.rubith.bot.command.annotation.Option;
import com.rubith.bot.command.impl.TestCommands;
import com.rubith.bot.command.utils.Command;
import com.rubith.bot.command.utils.CommandOption;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * This is my first time doing Annotations like actually using them.
 */
public class CommandHandler extends ListenerAdapter {
    private final Map<String, Command> commandMethods;

    private final JDA jda;

    public CommandHandler(JDA jda) {
        this.jda = jda;
        commandMethods = new HashMap<>();

        registerCommands(new TestCommands());
    }

    public void registerCommands(Object instance) {
        for (Method method : instance.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(CommandInfo.class)) {
                CommandInfo slashCommand = method.getAnnotation(CommandInfo.class);
                CommandDataImpl commandData = new CommandDataImpl(slashCommand.name(), slashCommand.description());
                List<CommandOption> options = new ArrayList<>();

                Parameter[] parameters = method.getParameters();
                for (Parameter parameter : parameters) {
                    if (parameter.isAnnotationPresent(Option.class)) {
                        Option commandOption = parameter.getAnnotation(Option.class);
                        options.add(new CommandOption(
                                commandOption.name(),
                                commandOption.description(),
                                commandOption.type(),
                                commandOption.required()
                        ));
                        commandData.addOption(commandOption.type(), commandOption.name(), commandOption.description(), commandOption.required());
                    }
                }

                Command command = new Command(
                        slashCommand.name(),
                        slashCommand.description(),
                        options,
                        Arrays.asList(slashCommand.permissions()),
                        method,
                        instance.getClass()
                );

                commandMethods.put(slashCommand.name(), command);
                commandData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(slashCommand.permissions()));

                try {
                    jda.upsertCommand(commandData).queue();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        if (commandMethods.containsKey(commandName)) {
            Command command = commandMethods.get(commandName);
            try {
                command.execute(event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }


}
