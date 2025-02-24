package ru.anton_flame.afitemseffects.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class ConfigManager {

    public static String effectInfoFormat, effectInLoreFormat, noPermission, reloaded, playerOnly, haventItem, incorrectEffect, incorrectLevel, highEffectLevel,
            highEffectAlreadyAdded, effectAdded, effectAlreadyAdded, economyNotFound, notEnoughCurrency, effectNotFound, noEffects, effectRemoved, allEffectsRemoved;
    public static List<String> adminHelp, effectsInfo, playerHelp;
    public static ConfigurationSection effects, effectLevels;

    public static void setupConfigValues(Plugin plugin) {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection settings = config.getConfigurationSection("settings");
        ConfigurationSection messages = config.getConfigurationSection("messages");

        effectInfoFormat = Hex.color(settings.getString("effect-info-format"));
        effectInLoreFormat = Hex.color(settings.getString("effect-in-lore-format"));
        effectLevels = settings.getConfigurationSection("effect-levels");
        effects = settings.getConfigurationSection("effects");
        noPermission = Hex.color(messages.getString("no-permission"));
        reloaded = Hex.color(messages.getString("reloaded"));
        playerOnly = Hex.color(messages.getString("player-only"));
        haventItem = Hex.color(messages.getString("havent-item"));
        incorrectEffect = Hex.color(messages.getString("incorrect-effect"));
        incorrectLevel = Hex.color(messages.getString("incorrect-level"));
        highEffectLevel = Hex.color(messages.getString("high-effect-level"));
        highEffectAlreadyAdded = Hex.color(messages.getString("high-effect-already-added"));
        effectAdded = Hex.color(messages.getString("effect-added"));
        effectAlreadyAdded = Hex.color(messages.getString("effect-already-added"));
        economyNotFound = Hex.color(messages.getString("economy-not-found"));
        notEnoughCurrency = Hex.color(messages.getString("not-enough-currency"));
        effectNotFound = Hex.color(messages.getString("effect-not-found"));
        noEffects = Hex.color(messages.getString("no-effects"));
        effectRemoved = Hex.color(messages.getString("effect-removed"));
        allEffectsRemoved = Hex.color(messages.getString("all-effects-removed"));
        adminHelp = Hex.color(messages.getStringList("admin-help"));
        effectsInfo = Hex.color(messages.getStringList("effects-info"));
        playerHelp = Hex.color(messages.getStringList("player-help"));
    }
}
