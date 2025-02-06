package ru.anton_flame.afitemseffects.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class ConfigManager {

    public static String effectInfoFormat, effectInLoreFormat, noPermission, reloaded, playerOnly, haventItem, incorrectEffect, incorrectLevel, highEffectLevel,
            highEffectAlreadyAdded, effectAdded, effectAlreadyAdded, economyNotFound, notEnoughCurrency, effectNotFound, noEffects, effectRemoved, allEffectsRemoved;
    public static List<String> adminHelp, effectsInfo, playerHelp;
    public static ConfigurationSection effects;

    public static void setupConfigValues(Plugin plugin) {
        effectInfoFormat = plugin.getConfig().getString("settings.effect-info-format");
        effectInLoreFormat = plugin.getConfig().getString("settings.effect-in-lore-format");
        noPermission = plugin.getConfig().getString("messages.no-permission");
        reloaded = plugin.getConfig().getString("messages.reloaded");
        playerOnly = plugin.getConfig().getString("messages.player-only");
        haventItem = plugin.getConfig().getString("messages.havent-item");
        incorrectEffect = plugin.getConfig().getString("messages.incorrect-effect");
        incorrectLevel = plugin.getConfig().getString("messages.incorrect-level");
        highEffectLevel = plugin.getConfig().getString("messages.high-effect-level");
        highEffectAlreadyAdded = plugin.getConfig().getString("messages.high-effect-already-added");
        effectAdded = plugin.getConfig().getString("messages.effect-added");
        effectAlreadyAdded = plugin.getConfig().getString("messages.effect-already-added");
        economyNotFound = plugin.getConfig().getString("messages.economy-not-found");
        notEnoughCurrency = plugin.getConfig().getString("messages.not-enough-currency");
        effectNotFound = plugin.getConfig().getString("messages.effect-not-found");
        noEffects = plugin.getConfig().getString("messages.no-effects");
        effectRemoved = plugin.getConfig().getString("messages.effect-removed");
        allEffectsRemoved = plugin.getConfig().getString("messages.all-effects-removed");
        adminHelp = plugin.getConfig().getStringList("messages.admin-help");
        effectsInfo = plugin.getConfig().getStringList("messages.effects-info");
        playerHelp = plugin.getConfig().getStringList("messages.player-help");
        effects = plugin.getConfig().getConfigurationSection("settings.effects");

    }
}
