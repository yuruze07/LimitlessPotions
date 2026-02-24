package com.fawwa.limitlesspotions.listeners;

import com.fawwa.limitlesspotions.LimitlessPotions;
import com.fawwa.limitlesspotions.utils.PotionCalculator;
import com.fawwa.limitlesspotions.utils.PotionValidator;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;

public class BrewingListener implements Listener {
    
    @EventHandler
    public void onBrew(BrewEvent event) {
        BrewerInventory inventory = event.getContents();
        ItemStack ingredient = inventory.getIngredient();
        
        if (ingredient == null) return;
        
        boolean isLevelUp = ingredient.getType() == Material.GLOWSTONE_DUST;
        boolean isDurationUp = ingredient.getType() == Material.REDSTONE;
        boolean isGunpowder = ingredient.getType() == Material.GUNPOWDER;
        boolean isDragonBreath = ingredient.getType() == Material.DRAGON_BREATH;
        
        // Handle gunpowder (splash)
        if (isGunpowder) {
            handleGunpowder(event, inventory);
            return;
        }
        
        // Handle dragon breath (lingering)
        if (isDragonBreath) {
            handleDragonBreath(event, inventory);
            return;
        }
        
        // Handle level/duration upgrade untuk SEMUA jenis potion
        if (!isLevelUp && !isDurationUp) return;
        
        // CANCEL EVENT
        event.setCancelled(true);
        
        // Proses brewing kita
        boolean success = false;
        
        for (int i = 0; i < 3; i++) {
            ItemStack bottle = inventory.getItem(i);
            if (bottle == null) continue;
            
            // Cek apakah ini potion (biasa, splash, atau lingering)
            boolean isValidPotion = bottle.getType() == Material.POTION || 
                                   bottle.getType() == Material.SPLASH_POTION || 
                                   bottle.getType() == Material.LINGERING_POTION;
            
            if (!isValidPotion) continue;
            
            if (!PotionValidator.isValidPotion(bottle)) continue;
            
            // Baca data potion
            int currentLevel = PotionValidator.getCurrentLevel(bottle);
            double currentDurationDouble = PotionValidator.getCurrentDurationDouble(bottle);
            String baseType = PotionValidator.getBasePotionType(bottle);
            
            System.out.println("Slot " + i + " - Data sebelum upgrade: Level " + currentLevel + ", Duration " + String.format("%.2f", currentDurationDouble) + "s");
            
            if (isLevelUp) {
                if (!PotionCalculator.canUpgradeLevel(currentLevel + 1)) {
                    // Hapus pesan chat
                    continue;
                }
                
                double newDuration = PotionCalculator.calculateNewDurationForLevelUp(currentDurationDouble);
                int newLevel = currentLevel + 1;
                
                // Simpan data baru (pertahankan tipe item yang sama)
                PotionValidator.savePotionData(bottle, newLevel, newDuration, baseType);
                inventory.setItem(i, bottle);
                
                // Hapus sendMessage
                success = true;
                
            } else if (isDurationUp) {
                if (!PotionCalculator.canAddDuration(currentDurationDouble)) {
                    // Hapus pesan chat
                    continue;
                }
                
                double bonus = PotionCalculator.calculateDurationBonus(currentLevel, currentDurationDouble);
                double newDuration = currentDurationDouble + bonus;
                
                // Simpan data baru
                PotionValidator.savePotionData(bottle, currentLevel, newDuration, baseType);
                inventory.setItem(i, bottle);
                
                // Hapus sendMessage
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
    
    private void handleGunpowder(BrewEvent event, BrewerInventory inventory) {
        boolean success = false;
        
        for (int i = 0; i < 3; i++) {
            ItemStack bottle = inventory.getItem(i);
            if (bottle == null || bottle.getType() != Material.POTION) continue;
            
            if (!PotionValidator.isValidPotion(bottle)) continue;
            
            // Baca data potion
            int level = PotionValidator.getCurrentLevel(bottle);
            double duration = PotionValidator.getCurrentDurationDouble(bottle);
            String baseType = PotionValidator.getBasePotionType(bottle);
            
            // Buat splash potion version
            ItemStack splashPotion = new ItemStack(Material.SPLASH_POTION, 1);
            
            // Copy semua data ke splash potion
            PotionValidator.savePotionData(splashPotion, level, duration, baseType);
            
            inventory.setItem(i, splashPotion);
            success = true;
        }
        
        if (success) {
            event.setCancelled(true);
            ItemStack ingredient = inventory.getIngredient();
            ingredient.setAmount(ingredient.getAmount() - 1);
            inventory.setIngredient(ingredient);
            inventory.getHolder().update();
        }
    }
    
    private void handleDragonBreath(BrewEvent event, BrewerInventory inventory) {
        boolean success = false;
        
        for (int i = 0; i < 3; i++) {
            ItemStack bottle = inventory.getItem(i);
            if (bottle == null) continue;
            
            // Bisa dari potion biasa atau splash potion
            boolean isValidSource = bottle.getType() == Material.POTION || 
                                   bottle.getType() == Material.SPLASH_POTION;
            
            if (!isValidSource) continue;
            
            if (!PotionValidator.isValidPotion(bottle)) continue;
            
            // Baca data potion
            int level = PotionValidator.getCurrentLevel(bottle);
            double duration = PotionValidator.getCurrentDurationDouble(bottle);
            String baseType = PotionValidator.getBasePotionType(bottle);
            
            // Buat lingering potion
            ItemStack lingeringPotion = new ItemStack(Material.LINGERING_POTION, 1);
            
            // Copy data
            PotionValidator.savePotionData(lingeringPotion, level, duration, baseType);
            
            inventory.setItem(i, lingeringPotion);
            success = true;
        }
        
        if (success) {
            event.setCancelled(true);
            ItemStack ingredient = inventory.getIngredient();
            ingredient.setAmount(ingredient.getAmount() - 1);
            inventory.setIngredient(ingredient);
            inventory.getHolder().update();
        }
    }
    
    @EventHandler
    public void onFuel(BrewingStandFuelEvent event) {
        // Biarkan event berjalan normal
    }
}