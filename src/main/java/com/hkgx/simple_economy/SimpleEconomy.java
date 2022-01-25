package com.hkgx.simple_economy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class SimpleEconomy implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("simple_economy");

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(SimpleEconomyCommands::registerCommands);
    }
}
