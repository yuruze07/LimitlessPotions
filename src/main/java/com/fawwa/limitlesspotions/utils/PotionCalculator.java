package com.fawwa.limitlesspotions.utils;

import com.fawwa.limitlesspotions.config.ConfigManager;
import com.fawwa.limitlesspotions.LimitlessPotions;

public class PotionCalculator {
    
    public static double calculateNewDurationForLevelUp(double currentDuration) {
        double percent = LimitlessPotions.getInstance()
                .getConfigManager()
                .getDurationRemainingPercent();
        
        return Math.max(0.1, currentDuration * (percent / 100.0));
    }
    
    public static double calculateDurationBonus(int currentLevel, double currentDuration) {
        ConfigManager config = LimitlessPotions.getInstance().getConfigManager();
        
        double baseBonus = config.getBaseBonusSeconds();
        double levelFactor = config.getLevelDiminishFactor();
        double durationFactor = config.getDurationDiminishFactor();
        
        // RUMUS: bonus = baseBonus / (level^levelFactor) / (log(duration+2)^durationFactor)
        
        double levelDivisor = Math.pow(currentLevel, levelFactor);
        double durationDivisor = Math.pow(Math.log(currentDuration + 2), durationFactor);
        
        double bonus = baseBonus / (levelDivisor * durationDivisor);
        
        // Minimal 0.1 detik, maksimal baseBonus
        bonus = Math.max(0.1, Math.min(bonus, baseBonus));
        
        // Debug
        System.out.println("  [DEBUG] Level: " + currentLevel + 
                         ", Durasi: " + String.format("%.2f", currentDuration) + 
                         ", LevelDiv: " + String.format("%.3f", levelDivisor) +
                         ", DurDiv: " + String.format("%.3f", durationDivisor) +
                         ", Bonus: " + String.format("%.2f", bonus));
        
        return bonus;
    }
    
    public static boolean canUpgradeLevel(int currentLevel) {
        ConfigManager config = LimitlessPotions.getInstance().getConfigManager();
        
        if (config.isUnlimitedLevels()) {
            return true;
        }
        
        return currentLevel < config.getMaxLevel();
    }
    
    public static boolean canAddDuration(double currentDuration) {
        ConfigManager config = LimitlessPotions.getInstance().getConfigManager();
        
        if (config.getMaxDuration() <= 0) {
            return true;
        }
        
        return currentDuration < config.getMaxDuration();
    }
}