package ru.anton_flame.afitemseffects;

import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ru.anton_flame.afitemseffects.commands.AFItemsEffectsCommand;
import ru.anton_flame.afitemseffects.commands.ItemsEffectsCommand;
import ru.anton_flame.afitemseffects.tasks.EffectsUpdateTask;
import ru.anton_flame.afitemseffects.utils.ConfigManager;

public final class AFItemsEffects extends JavaPlugin {

    public final NamespacedKey itemEffectsKey = new NamespacedKey(this, "item_effects");
    public final NamespacedKey itemIdentifierKey = new NamespacedKey(this, "item_identifier");
    public final NamespacedKey playerEffectsKey = new NamespacedKey(this, "player_effects");

    public PlayerPointsAPI playerPointsAPI;
    public Economy vaultAPI;

    @Override
    public void onEnable() {
        getLogger().info("Плагин был включен!");
        saveDefaultConfig();
        ConfigManager.setupConfigValues(this);
        if (!setupEconomy()) {
            getLogger().severe("Ни один плагин на экономику не был найден. Плагин будет выключен!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        PluginCommand afItemsEffectsCommand = getCommand("afitemseffects");
        AFItemsEffectsCommand afItemsEffectsCommandClass = new AFItemsEffectsCommand(this);
        afItemsEffectsCommand.setExecutor(afItemsEffectsCommandClass);
        afItemsEffectsCommand.setTabCompleter(afItemsEffectsCommandClass);

        PluginCommand itemsEffectsCommand = getCommand("itemseffects");
        ItemsEffectsCommand itemsEffectsCommandClass = new ItemsEffectsCommand(this);
        itemsEffectsCommand.setExecutor(itemsEffectsCommandClass);
        itemsEffectsCommand.setTabCompleter(itemsEffectsCommandClass);

        EffectsUpdateTask effectsUpdateTask = new EffectsUpdateTask(this);
        effectsUpdateTask.runTaskTimerAsynchronously(this, 0, 20);
    }

    @Override
    public void onDisable() {
        getLogger().info("Плагин был выключен!");
    }

    private boolean setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        vaultAPI = rsp.getProvider();
        return true;
    }

    private boolean setupEconomy() {
        boolean isPlayerPointsEnabled = false;
        boolean isVaultEnabled = false;

        if (Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            playerPointsAPI = PlayerPoints.getInstance().getAPI();
            isPlayerPointsEnabled = true;
            getLogger().info("Плагин PlayerPoints найден! Экономика с ним работать будет!");
        } else {
            getLogger().warning("Плагин PlayerPoints не найден! Экономика с ним работать не будет!");
        }

        if (setupVault()) {
            isVaultEnabled = true;
            getLogger().info("Плагин Vault найден! Экономика с ним работать будет!");
        } else {
            getLogger().warning("Плагин Vault не найден! Экономика с ним работать не будет!");
        }

        return isPlayerPointsEnabled || isVaultEnabled;
    }
}
