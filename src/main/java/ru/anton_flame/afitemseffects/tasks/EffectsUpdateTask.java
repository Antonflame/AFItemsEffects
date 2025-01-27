package ru.anton_flame.afitemseffects.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import ru.anton_flame.afitemseffects.AFItemsEffects;

import java.util.*;
import java.util.stream.Collectors;

public class EffectsUpdateTask extends BukkitRunnable {

    private final AFItemsEffects plugin;
    public EffectsUpdateTask(AFItemsEffects plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Map<String, String> currentEffects = getCurrentEffects(player);
            Map<String, String> newEffects = new HashMap<>();

            checkEffects(player.getInventory().getItemInMainHand(), newEffects);
            checkEffects(player.getInventory().getItemInOffHand(), newEffects);
            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (armor != null) {
                    checkEffects(armor, newEffects);
                }
            }

            for (Map.Entry<String, String> effectEntry : currentEffects.entrySet()) {
                String effectInfo = effectEntry.getKey();
                if (!newEffects.containsKey(effectInfo)) {
                    PotionEffectType type = PotionEffectType.getByName(effectInfo.split(":")[0]);
                    if (type != null) {
                        Bukkit.getScheduler().runTask(plugin, () -> player.removePotionEffect(type));
                    }
                }
            }

            for (Map.Entry<String, String> effectEntry : newEffects.entrySet()) {
                String effectInfo = effectEntry.getKey();
                String identifier = effectEntry.getValue();
                if (!currentEffects.containsKey(effectInfo) || !currentEffects.get(effectInfo).equals(identifier)) {
                    String[] effectParts = effectInfo.split(":");
                    PotionEffectType type = PotionEffectType.getByName(effectParts[0]);
                    int level = Integer.parseInt(effectParts[1]) - 1;

                    if (type != null) {
                        boolean shouldUpdate = false;
                        PotionEffect existingEffect = null;
                        for (PotionEffect activeEffect : player.getActivePotionEffects()) {
                            if (activeEffect.getType() == type && activeEffect.getAmplifier() == level) {
                                existingEffect = activeEffect;
                                if (activeEffect.getDuration() < Integer.MAX_VALUE) {
                                    shouldUpdate = true;
                                }
                                break;
                            }
                        }

                        if (shouldUpdate || existingEffect == null) {
                            Bukkit.getScheduler().runTask(plugin, () -> player.addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, level)));
                        }
                    }
                }
            }
            saveCurrentEffects(player, newEffects);
        }
    }

    private Map<String, String> getCurrentEffects(Player player) {
        Map<String, String> effects = new HashMap<>();
        if (player.getPersistentDataContainer().has(plugin.playerEffectsKey, PersistentDataType.STRING)) {
            String savedEffects = player.getPersistentDataContainer().get(plugin.playerEffectsKey, PersistentDataType.STRING);
            for (String effectEntry : savedEffects.split(";")) {
                String[] parts = effectEntry.split(":");
                if (parts.length == 3) {
                    String effectType = parts[0];
                    String effectLevel = parts[1];
                    String identifier = parts[2];
                    effects.put(effectType + ":" + effectLevel, identifier);
                }
            }
        }
        return effects;
    }

    private void saveCurrentEffects(Player player, Map<String, String> effects) {
        String effectsString = effects.entrySet().stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .collect(Collectors.joining(";"));
        player.getPersistentDataContainer().set(plugin.playerEffectsKey, PersistentDataType.STRING, effectsString);
    }

    private void checkEffects(ItemStack item, Map<String, String> effects) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(plugin.itemEffectsKey, PersistentDataType.STRING)) return;

        String itemEffectsString = container.get(plugin.itemEffectsKey, PersistentDataType.STRING);
        if (itemEffectsString == null || itemEffectsString.isEmpty()) return;

        List<String> itemEffectsList = Arrays.asList(itemEffectsString.split(";"));
        for (String effect : itemEffectsList) {
            String[] effectInfo = effect.split(":");
            if (effectInfo.length == 2) {
                String effectType = effectInfo[0];
                int effectLevel = Integer.parseInt(effectInfo[1]);
                String identifier = generateIdentifierKey(item);
                effects.put(effectType + ":" + effectLevel, identifier);
            }
        }
    }

    private String generateIdentifierKey(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return "";
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (!container.has(plugin.itemIdentifierKey, PersistentDataType.STRING)) {
            String uniqueId = UUID.randomUUID().toString();
            container.set(plugin.itemIdentifierKey, PersistentDataType.STRING, uniqueId);
            item.setItemMeta(meta);
            return uniqueId;
        }

        return container.get(plugin.itemIdentifierKey, PersistentDataType.STRING);
    }
}
