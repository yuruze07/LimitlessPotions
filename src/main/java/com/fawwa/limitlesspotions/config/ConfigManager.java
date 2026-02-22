package com.fawwa.limitlesspotions.config;  // Ganti dari yourname ke fawwa

import com.fawwa.limitlesspotions.LimitlessPotions;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {
    
    private final LimitlessPotions plugin;
    private FileConfiguration config;
    
    // Level settings
    private double durationRemainingPercent;
    private boolean unlimitedLevels;
    private int maxLevel;
    
    // Duration settings
    private double baseAdditionSeconds;
    private double levelDiminishFactor;
    private double durationDiminishFactor;
    private int maxDuration;
    
    // Excluded potions
    private List<String> excludedPotions;
    
    public ConfigManager(LimitlessPotions plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        loadConfig();
    }
    
    public void loadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        
        // Load level settings
        durationRemainingPercent = config.getDouble("level-up.duration-remaining-percent", 15.0);
        unlimitedLevels = config.getBoolean("level-up.unlimited-levels", true);
        maxLevel = config.getInt("level-up.max-level", 10);
        
        // Load duration settings
        baseAdditionSeconds = config.getDouble("duration-up.base-addition-seconds", 30.0);
        levelDiminishFactor = config.getDouble("duration-up.level-diminish-factor", 0.7);
        durationDiminishFactor = config.getDouble("duration-up.duration-diminish-factor", 0.3);
        maxDuration = config.getInt("duration-up.max-duration", 0);
        
        // Load excluded potions
        excludedPotions = config.getStringList("excluded-potions");
    }
    
    public String getMessage(String key) {
        String message = config.getString("messages." + key, "&cMessage not found: " + key);
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    // Getters
    public double getDurationRemainingPercent() { return durationRemainingPercent; }
    public boolean isUnlimitedLevels() { return unlimitedLevels; }
    public int getMaxLevel() { return maxLevel; }
    public double getBaseAdditionSeconds() { return baseAdditionSeconds; }
    public double getLevelDiminishFactor() { return levelDiminishFactor; }
    public double getDurationDiminishFactor() { return durationDiminishFactor; }
    public int getMaxDuration() { return maxDuration; }
    public List<String> getExcludedPotions() { return excludedPotions; }
}