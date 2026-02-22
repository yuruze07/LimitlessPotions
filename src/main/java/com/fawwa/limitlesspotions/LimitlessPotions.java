package com.fawwa.limitlesspotions;  // Ganti dari yourname ke fawwa

import com.fawwa.limitlesspotions.config.ConfigManager;
import com.fawwa.limitlesspotions.listeners.BrewingListener;
import org.bukkit.plugin.java.JavaPlugin;

public class LimitlessPotions extends JavaPlugin {
    
    private static LimitlessPotions instance;
    private ConfigManager configManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Load config
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        
        // Register listener
        getServer().getPluginManager().registerEvents(new BrewingListener(), this);
        
        getLogger().info("LimitlessPotions has been enabled!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("LimitlessPotions has been disabled!");
    }
    
    public static LimitlessPotions getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
}