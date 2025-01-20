package ru.anton_flame.afitemseffects.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.anton_flame.afitemseffects.AFItemsEffects;
import ru.anton_flame.afitemseffects.utils.Hex;

public class AFItemsEffectsCommand implements CommandExecutor {

    private final AFItemsEffects plugin;
    public AFItemsEffectsCommand(AFItemsEffects plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length < 1 || !strings[0].equalsIgnoreCase("reload")) {
            for (String message : plugin.getConfig().getStringList("messages.admin-help")) {
                commandSender.sendMessage(Hex.color(message));
            }
            return false;
        }

        if (strings.length == 1 && strings[0].equalsIgnoreCase("reload")) {
            if (!commandSender.hasPermission("afitemseffects.reload")) {
                commandSender.sendMessage(Hex.color(plugin.getConfig().getString("messages.no-permission")));
                return false;
            }

            plugin.reloadConfig();
            commandSender.sendMessage(Hex.color(plugin.getConfig().getString("messages.reloaded")));
        }
        return true;
    }
}
