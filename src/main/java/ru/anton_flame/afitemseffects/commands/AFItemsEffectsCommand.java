package ru.anton_flame.afitemseffects.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.anton_flame.afitemseffects.AFItemsEffects;
import ru.anton_flame.afitemseffects.utils.ConfigManager;

import java.util.Collections;
import java.util.List;

public class AFItemsEffectsCommand implements CommandExecutor, TabCompleter {

    private final AFItemsEffects plugin;
    public AFItemsEffectsCommand(AFItemsEffects plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length < 1 || !strings[0].equalsIgnoreCase("reload")) {
            for (String message : ConfigManager.adminHelp) {
                commandSender.sendMessage(message);
            }
            return false;
        }

        if (strings.length == 1 && strings[0].equalsIgnoreCase("reload")) {
            if (!commandSender.hasPermission("afitemseffects.reload")) {
                commandSender.sendMessage(ConfigManager.noPermission);
                return false;
            }

            plugin.reloadConfig();
            ConfigManager.setupConfigValues(plugin);
            commandSender.sendMessage(ConfigManager.reloaded);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1) {
            return Collections.singletonList("reload");
        }

        return Collections.emptyList();
    }
}
