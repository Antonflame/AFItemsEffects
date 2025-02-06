package ru.anton_flame.afitemseffects.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.anton_flame.afitemseffects.AFItemsEffects;
import ru.anton_flame.afitemseffects.utils.ConfigManager;
import ru.anton_flame.afitemseffects.utils.Hex;

import java.util.*;

public class ItemsEffectsCommand implements CommandExecutor, TabCompleter {

    private final AFItemsEffects plugin;
    public ItemsEffectsCommand(AFItemsEffects plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Hex.color(ConfigManager.playerOnly));
            return false;
        }

        Player player = (Player) commandSender;

        if (strings.length < 1) {
            for (String message : ConfigManager.playerHelp) {
                player.sendMessage(Hex.color(message));
            }
            return false;
        }

        switch (strings[0].toLowerCase()) {
            case "add": {
                if (strings.length != 3) {
                    for (String message : ConfigManager.playerHelp) {
                        player.sendMessage(Hex.color(message));
                    }
                    return false;
                }

                if (!player.hasPermission("afitemseffects.add")) {
                    player.sendMessage(Hex.color(ConfigManager.noPermission));
                    return false;
                }

                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() == Material.AIR) {
                    player.sendMessage(Hex.color(ConfigManager.haventItem));
                    return false;
                }

                PotionEffectType effectType = PotionEffectType.getByName(strings[1].toUpperCase());
                int effectLevel;

                if (effectType == null) {
                    player.sendMessage(Hex.color(ConfigManager.incorrectEffect));
                    return false;
                }

                ConfigurationSection effectsSection = ConfigManager.effects;
                ConfigurationSection levelsSection = effectsSection.getConfigurationSection(effectType.getName().toUpperCase() + ".levels");

                for (String potionEffectType : effectsSection.getKeys(false)) {
                    if (effectType.getName().equalsIgnoreCase(potionEffectType)) {
                        break;
                    }
                }

                try {
                    effectLevel = Integer.parseInt(strings[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(Hex.color(ConfigManager.incorrectLevel));
                    return false;
                }

                int max = 0;
                for (String key : levelsSection.getKeys(false)) {
                    if (Integer.parseInt(key) > max) {
                        max = Integer.parseInt(key);
                    }
                }

                if (effectLevel > max) {
                    player.sendMessage(Hex.color(ConfigManager.highEffectLevel));
                    return false;
                }

                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();

                @Nullable List<Component> lore = meta.lore();
                if (lore == null) lore = new ArrayList<>();

                String currentEffects = container.getOrDefault(plugin.itemEffectsKey, PersistentDataType.STRING, "");
                String effectInfo = effectType.getName() + ":" + effectLevel;
                List<String> effects = new ArrayList<>(Arrays.asList(currentEffects.split(";")));

                if (!effects.isEmpty()) {
                    if (effects.contains(effectInfo)) {
                        player.sendMessage(Hex.color(ConfigManager.effectAlreadyAdded.replace("%effect%", effectType.getName())));
                        return false;
                    }

                    for (String effect : effects) {
                        String[] effectSplit = effect.split(":");
                        if (effectSplit.length == 2) {
                            String currentEffectType = effectSplit[0];
                            int currentEffectLevel = Integer.parseInt(effectSplit[1]);

                            if (currentEffectType.equalsIgnoreCase(effectType.getName()) && currentEffectLevel < effectLevel) {
                                String format = Hex.color(ConfigManager.effectAlreadyAdded
                                        .replace("%effect_type%", effectsSection.getString(currentEffectType + ".display-name"))
                                        .replace("%effect_level%", String.valueOf(currentEffectLevel)));
                                lore.remove(Component.text(format));
                                meta.lore(lore);
                                item.setItemMeta(meta);

                                effects.remove(effect);
                                break;
                            }

                            if (currentEffectType.equalsIgnoreCase(effectType.getName()) && currentEffectLevel > effectLevel) {
                                player.sendMessage(Hex.color(ConfigManager.highEffectAlreadyAdded));
                                return false;
                            }
                        }
                    }
                }

                String economyName = levelsSection.getString(effectLevel + ".economy");
                int price = levelsSection.getInt(effectLevel + ".price");

                if (economyName != null && price > 0) {
                    if (economyName.equalsIgnoreCase("Vault")) {
                        if (plugin.vaultAPI == null) {
                            player.sendMessage(Hex.color(ConfigManager.economyNotFound));
                            return false;
                        }

                        if (!plugin.vaultAPI.has(player, price)) {
                            player.sendMessage(Hex.color(ConfigManager.notEnoughCurrency));
                            return false;
                        }

                        plugin.vaultAPI.withdrawPlayer(player, price);
                    } else if (economyName.equalsIgnoreCase("PlayerPoints")) {
                        if (plugin.playerPointsAPI == null) {
                            player.sendMessage(Hex.color(ConfigManager.economyNotFound));
                            return false;
                        }

                        if (plugin.playerPointsAPI.look(player.getUniqueId()) < price) {
                            player.sendMessage(Hex.color(ConfigManager.notEnoughCurrency));
                            return false;
                        }

                        plugin.playerPointsAPI.take(player.getUniqueId(), price);
                    }

                    effects.add(effectInfo);
                    String serializedEffects = String.join(";", effects);
                    container.set(plugin.itemEffectsKey, PersistentDataType.STRING, serializedEffects);
                    item.setItemMeta(meta);
                    player.addPotionEffect(new PotionEffect(effectType, Integer.MAX_VALUE, effectLevel - 1));

                    for (String effect : effects) {
                        String[] effectSplit = effect.split(":");
                        if (effectSplit.length == 2) {
                            String currentEffectName = effectsSection.getString(effectSplit[0] + ".display-name");
                            String currentEffectLevel = effectSplit[1];

                            if (effectSplit[0].equalsIgnoreCase(effectType.getName())) {
                                String format = Hex.color(ConfigManager.effectInLoreFormat
                                        .replace("%effect_type%", currentEffectName)
                                        .replace("%effect_level%", currentEffectLevel));
                                lore.add(Component.text(format));
                            }
                        }
                    }
                    meta.lore(lore);
                    item.setItemMeta(meta);

                    player.sendMessage(Hex.color(ConfigManager.effectAdded.replace("%effect%", effectType.getName())));
                }
            }
            break;

            case "remove": {
                if (strings.length != 2) {
                    for (String message : ConfigManager.playerHelp) {
                        player.sendMessage(Hex.color(message));
                    }
                    return false;
                }

                if (!player.hasPermission("afitemseffects.remove")) {
                    player.sendMessage(Hex.color(ConfigManager.noPermission));
                    return false;
                }

                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() == Material.AIR) {
                    player.sendMessage(Hex.color(ConfigManager.haventItem));
                    return false;
                }

                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();

                @Nullable List<Component> lore = meta.lore();
                if (lore == null) lore = new ArrayList<>();

                String itemEffects = container.getOrDefault(plugin.itemEffectsKey, PersistentDataType.STRING, "");
                List<String> effects = new ArrayList<>(Arrays.asList(itemEffects.split(";")));

                if (!strings[1].equalsIgnoreCase("all")) {
                    if (effects.isEmpty()) {
                        player.sendMessage(Hex.color(ConfigManager.noEffects));
                        return false;
                    }

                    boolean found = false;
                    String effectType = strings[1];
                    for (String effect : effects) {
                        String[] effectSplit = effect.split(":");
                        String effectName = effectSplit[0];

                        if (effectName.equalsIgnoreCase(effectType)) {
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        player.sendMessage(Hex.color(ConfigManager.effectNotFound.replace("%effect%", effectType)));
                        return false;
                    }

                    for (String effect : effects) {
                        String[] effectSplit = effect.split(":");
                        if (effectSplit.length == 2) {
                            String effectName = effectSplit[0];
                            String effectLevel = effectSplit[1];

                            if (effectName.equalsIgnoreCase(effectType)) {
                                effects.remove(effect);
                                container.set(plugin.itemEffectsKey, PersistentDataType.STRING, String.join(";", effects));
                                item.setItemMeta(meta);

                                String format = Hex.color(ConfigManager.effectInLoreFormat
                                        .replace("%effect_type%", ConfigManager.effects.getString(effectName + ".display-name"))
                                        .replace("%effect_level%", effectLevel));
                                lore.remove(Component.text(format));

                                meta.lore(lore);
                                item.setItemMeta(meta);

                                player.sendMessage(Hex.color(ConfigManager.effectRemoved).replace("%effect%", effectName));
                                break;
                            }
                        }
                    }
                } else {
                    if (itemEffects.isEmpty()) {
                        player.sendMessage(Hex.color(ConfigManager.noEffects));
                        return false;
                    }

                    for (String effect : effects) {
                        String[] effectSplit = effect.split(":");
                        if (effectSplit.length == 2) {
                            String currentEffectName = effectSplit[0];
                            String currentEffectLevel = effectSplit[1];

                            String format = Hex.color(ConfigManager.effectInLoreFormat
                                    .replace("%effect_type%", ConfigManager.effects.getString(currentEffectName + ".display-name"))
                                    .replace("%effect_level%", currentEffectLevel));

                            lore.remove(Component.text(format));

                            meta.lore(lore);
                            item.setItemMeta(meta);
                        }
                    }

                    container.set(plugin.itemEffectsKey, PersistentDataType.STRING, "");
                    item.setItemMeta(meta);

                    player.sendMessage(Hex.color(ConfigManager.allEffectsRemoved));
                }
            }
            break;

            case "info": {
                if (strings.length != 1) {
                    for (String message : ConfigManager.playerHelp) {
                        player.sendMessage(Hex.color(message));
                    }
                    return false;
                }

                if (!player.hasPermission("afitemseffects.info")) {
                    player.sendMessage(Hex.color(ConfigManager.noPermission));
                    return false;
                }

                ConfigurationSection effectsSection = ConfigManager.effects;
                List<String> effects = new ArrayList<>();
                for (String effect : effectsSection.getKeys(false)) {
                    for (String effectLevel : effectsSection.getConfigurationSection(effect + ".levels").getKeys(false)) {
                        String format = Hex.color(ConfigManager.effectInfoFormat
                                .replace("%effect_type%", effect)
                                .replace("%effect_display_name%", effectsSection.getString(effect + ".display-name"))
                                .replace("%effect_level%", effectLevel)
                                .replace("%effect_level_price%", effectsSection.getString(effect + ".levels." + effectLevel + ".price"))
                                .replace("%effect_economy_display_name%", effectsSection.getString(effect + ".levels." + effectLevel + ".economy-display-name")));
                        effects.add(format);
                    }
                }
                String effectsString = String.join("\n", effects);
                for (String message : ConfigManager.effectsInfo) {
                    player.sendMessage(Hex.color(message).replace("%effects%", effectsString));
                }
            }
            break;

            default:
                for (String message : ConfigManager.playerHelp) {
                    player.sendMessage(Hex.color(message));
                }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1) {
            return Arrays.asList("add", "remove", "remove all", "info");
        }

        if (strings[0].equalsIgnoreCase("add")) {
            if (strings.length == 2) {
                return new ArrayList<>(ConfigManager.effects.getKeys(false));
            }

            if (strings.length == 3) {
                ConfigurationSection levelsSection = ConfigManager.effects.getConfigurationSection(strings[1] + ".levels");
                if (levelsSection != null) {
                    return new ArrayList<>(levelsSection.getKeys(false));
                }
            }
        }

        if (strings[0].equalsIgnoreCase("remove")) {
            if (strings.length == 2) {
                List<String> effects = new ArrayList<>(ConfigManager.effects.getKeys(false));
                effects.add("all");
                return effects;
            }
        }

        return Collections.emptyList();
    }
}
