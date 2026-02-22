package com.fawwa.limitlesspotions.listeners;

import com.fawwa.limitlesspotions.LimitlessPotions;
import com.fawwa.limitlesspotions.utils.PotionCalculator;
import com.fawwa.limitlesspotions.utils.PotionValidator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class BrewingListener implements Listener {
    
    @EventHandler
    public void onBrew(BrewEvent event) {
        BrewerInventory inventory = event.getContents();
        ItemStack ingredient = inventory.getIngredient();
        
        if (ingredient == null) return;
        
        boolean isLevelUp = ingredient.getType() == Material.GLOWSTONE_DUST;
        boolean isDurationUp = ingredient.getType() == Material.REDSTONE;
        
        if (!isLevelUp && !isDurationUp) return;
        
        // CANCEL EVENT - Kita handle sendiri
        event.setCancelled(true);
        
        // Proses brewing kita
        boolean success = false;
        
        for (int i = 0; i < 3; i++) {
            ItemStack bottle = inventory.getItem(i);
            if (bottle == null || bottle.getType() != Material.POTION) continue;
            
            if (!PotionValidator.isValidPotion(bottle)) continue;
            
            // Baca data potion (selalu baca ulang dari item)
            int currentLevel = PotionValidator.getCurrentLevel(bottle);
            int currentDuration = PotionValidator.getCurrentDuration(bottle);
            String baseType = PotionValidator.getBasePotionType(bottle);
            
            System.out.println("Slot " + i + " - Data sebelum upgrade: Level " + currentLevel + ", Duration " + currentDuration);
            
            if (isLevelUp) {
                if (!PotionCalculator.canUpgradeLevel(currentLevel + 1)) {
                    sendMessage(inventory, "max-level");
                    continue;
                }
                
                int newDuration = PotionCalculator.calculateNewDurationForLevelUp(currentDuration);
                int newLevel = currentLevel + 1;
                
                // Buat item baru dan simpan data
                PotionValidator.savePotionData(bottle, newLevel, newDuration, baseType);
                inventory.setItem(i, bottle);
                
                sendMessage(inventory, "upgrade-level", "%level%", String.valueOf(newLevel));
                success = true;
                
            } else if (isDurationUp) {
                if (!PotionCalculator.canAddDuration(currentDuration + 1)) {
                    sendMessage(inventory, "max-duration");
                    continue;
                }
                
                int bonus = PotionCalculator.calculateDurationBonus(currentLevel, currentDuration);
                int newDuration = currentDuration + bonus;
                
                // Buat item baru dan simpan data
                PotionValidator.savePotionData(bottle, currentLevel, newDuration, baseType);
                inventory.setItem(i, bottle);
                
                sendMessage(inventory, "upgrade-duration", 
                           "%duration%", String.valueOf(bonus),
                           "%total%", String.valueOf(newDuration));
                success = true;
            }
        }
        
        if (success) {
            // Kurangi ingredient
            ingredient.setAmount(ingredient.getAmount() - 1);
            inventory.setIngredient(ingredient);
            
            // Update brewing stand
            inventory.getHolder().update();
        }
    }
    
    private void sendMessage(BrewerInventory inventory, String messageKey, String... replacements) {
        if (inventory.getHolder() == null) return;
        
        String message = LimitlessPotions.getInstance()
                .getConfigManager()
                .getMessage(messageKey);
        
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        
        final String finalMessage = message;
        inventory.getHolder().getWorld().getPlayers().forEach(player -> {
            if (player.getLocation().distance(inventory.getLocation()) < 5) {
                player.sendMessage(finalMessage);
            }
        });
    }
    
    @EventHandler
    public void onFuel(BrewingStandFuelEvent event) {
        // Biarkan event berjalan normal
    }
}