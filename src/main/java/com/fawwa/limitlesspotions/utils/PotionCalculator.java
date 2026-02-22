package com.fawwa.limitlesspotions.utils;

import com.fawwa.limitlesspotions.config.ConfigManager;
import com.fawwa.limitlesspotions.LimitlessPotions;

public class PotionCalculator {
    
    public static int calculateNewDurationForLevelUp(int currentDuration) {
        double percent = LimitlessPotions.getInstance()
                .getConfigManager()
                .getDurationRemainingPercent();
        
        return (int) Math.max(1, currentDuration * (percent / 100.0));
    }
    
    public static int calculateDurationBonus(int currentLevel, int currentDuration) {
        ConfigManager config = LimitlessPotions.getInstance().getConfigManager();
        
        double baseAddition = config.getBaseAdditionSeconds();
        double levelFactor = config.getLevelDiminishFactor();
        double durationFactor = config.getDurationDiminishFactor();
        
        // Rumus diminishing return yang lebih smooth
        // bonus = base * (1 / (1 + levelFactor * log(level))) * (1 / (1 + durationFactor * sqrt(duration)))
        double levelDiminish = 1.0 / (1.0 + (levelFactor * Math.log(currentLevel + 1)));
        double durationDiminish = 1.0 / (1.0 + (durationFactor * Math.sqrt(currentDuration / 10.0)));
        
        int bonus = (int) (baseAddition * levelDiminish * durationDiminish);
        
        // Minimal bonus 1 detik, maksimal base addition
        return Math.max(1, Math.min((int) baseAddition, bonus));
    }
    
    public static boolean canUpgradeLevel(int currentLevel) {
        ConfigManager config = LimitlessPotions.getInstance().getConfigManager();
        
        if (config.isUnlimitedLevels()) {
            return true;
        }
        
        return currentLevel < config.getMaxLevel();
    }
    
    public static boolean canAddDuration(int currentDuration) {
        ConfigManager config = LimitlessPotions.getInstance().getConfigManager();
        
        if (config.getMaxDuration() <= 0) {
            return true;
        }
        
        return currentDuration < config.getMaxDuration();
    }
}