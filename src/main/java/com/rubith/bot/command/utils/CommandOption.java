package com.rubith.bot.command.utils;

import net.dv8tion.jda.api.interactions.commands.OptionType;

public class CommandOption {
    private final String name;
    private final String description;
    private final OptionType type;
    private final boolean required;

    public CommandOption(String name, String description, OptionType type, boolean required) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public OptionType getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }
}