package ru.anton_flame.afitemseffects;

import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ru.anton_flame.afitemseffects.commands.AFItemsEffectsCommand;
import ru.anton_flame.afitemseffects.commands.ItemsEffectsCommand;
import ru.anton_flame.afitemseffects.tasks.EffectsUpdateTask;

public final class AFItemsEffects extends JavaPlugin {

    public final NamespacedKey itemEffectsKey = new NamespacedKey(this, "item_effects");

    public PlayerPointsAPI playerPointsAPI;
    public Economy vaultAPI;

    @Override
    public void onEnable() {
        getLogger().info("Плагин был включен!");
        saveDefaultConfig();
        setupEconomy();

        getCommand("afitemseffects").setExecutor(new AFItemsEffectsCommand(this));
        getCommand("itemseffects").setExecutor(new ItemsEffectsCommand(this));

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

    private void setupEconomy() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            playerPointsAPI = PlayerPoints.getInstance().getAPI();
            getLogger().info("Плагин PlayerPoints найден! Экономика с ним работать будет!");
        } else {
            getLogger().warning("Плагин PlayerPoints не найден! Экономика с этим плагином работать не будет!");
        }

        if (setupVault()) {
            getLogger().info("Плагин Vault найден! Экономика с этим плагином работать будет!");
        } else {
            getLogger().warning("Плагин Vault не найден! Экономика с ним работать не будет!");
        }
    }
}
