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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EffectsUpdateTask extends BukkitRunnable {

    private final AFItemsEffects plugin;
    public EffectsUpdateTask(AFItemsEffects plugin) {
        this.plugin = plugin;
    }

    private boolean hasEffect = false;

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            List<String> effects = new ArrayList<>();

            ItemStack mainHandItem = player.getInventory().getItemInMainHand();
            checkEffects(mainHandItem, effects);

            ItemStack offHandItem = player.getInventory().getItemInOffHand();
            checkEffects(offHandItem, effects);

            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (armor != null) checkEffects(armor, effects);
            }

            if (hasEffect) {
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    String effectInfo = effect.getType().getName() + ":" + (effect.getAmplifier() + 1);

                    if (!effects.contains(effectInfo)) {
                        Bukkit.getScheduler().runTask(plugin, () -> player.removePotionEffect(effect.getType()));
                    }
                }
            }

            for (String effect : effects) {
                String[] effectInfo = effect.split(":");
                if (effectInfo.length == 2) {
                    PotionEffectType effectType = PotionEffectType.getByName(effectInfo[0]);
                    int effectLevel = Integer.parseInt(effectInfo[1]);

                    if (!player.hasPotionEffect(effectType)) {
                        Bukkit.getScheduler().runTask(plugin, () -> player.addPotionEffect(new PotionEffect(effectType, Integer.MAX_VALUE, effectLevel - 1)));
                    }
                }
            }
        }
    }

    private void checkEffects(ItemStack item, List<String> effects) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();

        String itemEffects = container.getOrDefault(plugin.itemEffectsKey, PersistentDataType.STRING, "");
        List<String> itemEffectsList = new ArrayList<>(Arrays.asList(itemEffects.split(";")));

        if (item.getType() != Material.AIR) {
            for (String effect : itemEffectsList) {
                String[] effectInfo = effect.split(":");
                if (effectInfo.length == 2) {
                    String effectType = effectInfo[0];
                    int effectLevel = Integer.parseInt(effectInfo[1]);

                    boolean addEffect = true;
                    for (String itemEffect : effects) {
                        String[] effectSplit = itemEffect.split(":");
                        if (effectSplit.length == 2) {
                            String currentEffectType = effectSplit[0];
                            int currentEffectLevel = Integer.parseInt(effectSplit[1]);

                            if (currentEffectType.equalsIgnoreCase(effectType) && currentEffectLevel < effectLevel) {
                                effects.remove(effect);
                                break;
                            }

                            if (currentEffectType.equalsIgnoreCase(effectType) && currentEffectLevel > effectLevel) {
                                addEffect = false;
                                break;
                            }
                        }
                    }

                    if (addEffect) {
                        hasEffect = true;
                        effects.add(effectType + ":" + effectLevel);
                    }
                }
            }
        }
    }
}
