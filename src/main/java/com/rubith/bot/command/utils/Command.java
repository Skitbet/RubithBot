package com.rubith.bot.command.utils;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Command {
    private final String name;
    private final String description;
    private final List<CommandOption> options;
    private final List<Permission> permissions;
    private final Method method;
    private final Object object;

    public Command(String name, String description, List<CommandOption> options, List<Permission> permissions, Method method, Object object) {
        this.name = name;
        this.description = description;
        this.options = options;
        this.permissions = permissions;
        this.method = method;
        this.object = object;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }


    public void execute(SlashCommandInteractionEvent event) throws IllegalAccessException, InvocationTargetException {
        List<Object> arguments = new ArrayList<>();
        arguments.add(event);
        for (CommandOption option : options) {
            OptionType optionType = option.getType();
            String optionName = option.getName();

            if (event.getOption(optionName) == null) {
                if (option.getType() == OptionType.BOOLEAN) {
                    arguments.add(false);
                    continue;
                }
                arguments.add(null);
                continue;
            }


            switch (optionType) {
                case STRING:
                    arguments.add(event.getOption(optionName).getAsString());
                    break;
                case INTEGER:
                    arguments.add(event.getOption(optionName).getAsInt());
                    break;
                case BOOLEAN:
                    arguments.add(event.getOption(optionName).getAsBoolean());
                    break;
            }
        }

        method.invoke(object, arguments.toArray());
    }
}
