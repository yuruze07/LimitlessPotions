package com.fawwa.limitlesspotions.utils;

import com.fawwa.limitlesspotions.LimitlessPotions;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class PotionValidator {
    
    private static final NamespacedKey LEVEL_KEY = new NamespacedKey(LimitlessPotions.getInstance(), "potion_level");
    private static final NamespacedKey DURATION_KEY = new NamespacedKey(LimitlessPotions.getInstance(), "potion_duration_double");
    private static final NamespacedKey BASE_TYPE_KEY = new NamespacedKey(LimitlessPotions.getInstance(), "base_potion_type");
    private static final NamespacedKey UPGRADED_KEY = new NamespacedKey(LimitlessPotions.getInstance(), "is_upgraded");
    private static final NamespacedKey POTION_TYPE_KEY = new NamespacedKey(LimitlessPotions.getInstance(), "potion_type");
    
    // Constants untuk tipe potion
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_SPLASH = 1;
    public static final int TYPE_LINGERING = 2;
    
    public static boolean isValidPotion(ItemStack item) {
        if (item == null || !(item.getItemMeta() instanceof PotionMeta)) {
            return false;
        }
        
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        PotionData data = meta.getBasePotionData();
        
        // Check if potion type is excluded
        PotionType type = data.getType();
        String typeName = type.getKey().getKey().toLowerCase();
        
        return !LimitlessPotions.getInstance()
                .getConfigManager()
                .getExcludedPotions()
                .contains(typeName);
    }
    
    public static boolean isInstantPotion(PotionType type) {
        String typeName = type.getKey().getKey().toLowerCase();
        return typeName.contains("instant") || 
               typeName.equals("healing") || 
               typeName.equals("harming") ||
               typeName.equals("strong_healing") || 
               typeName.equals("strong_harming");
    }
    
    public static boolean hasDuration(PotionType type) {
        String typeName = type.getKey().getKey().toLowerCase();
        return !(typeName.contains("healing") || 
                typeName.contains("harming") || 
                typeName.contains("instant"));
    }
    
    public static boolean isCustomPotion(ItemStack item) {
        if (!(item.getItemMeta() instanceof PotionMeta)) return false;
        
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        boolean hasLevel = container.has(LEVEL_KEY, PersistentDataType.INTEGER);
        boolean hasUpgraded = container.has(UPGRADED_KEY, PersistentDataType.BOOLEAN);
        
        System.out.println("  [DEBUG isCustomPotion] hasLevel: " + hasLevel + ", hasUpgraded: " + hasUpgraded);
        
        return hasLevel || hasUpgraded;
    }
    
    public static int getCurrentLevel(ItemStack item) {
        if (!(item.getItemMeta() instanceof PotionMeta)) return 1;
        
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        Integer level = container.get(LEVEL_KEY, PersistentDataType.INTEGER);
        if (level != null) {
            System.out.println("  [DEBUG getCurrentLevel] dari persistent: " + level);
            return level;
        }
        
        PotionData data = meta.getBasePotionData();
        if (data.isUpgraded()) {
            System.out.println("  [DEBUG getCurrentLevel] dari vanilla upgraded: 2");
            return 2;
        }
        
        System.out.println("  [DEBUG getCurrentLevel] default: 1");
        return 1;
    }
    
    public static double getCurrentDurationDouble(ItemStack item) {
        if (!(item.getItemMeta() instanceof PotionMeta)) return 0;
        
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        Double duration = container.get(DURATION_KEY, PersistentDataType.DOUBLE);
        if (duration != null) {
            System.out.println("  [DEBUG getCurrentDurationDouble] dari persistent: " + duration + " detik (real)");
            return duration;
        }
        
        PotionData data = meta.getBasePotionData();
        PotionType type = data.getType();
        
        if (hasDuration(type)) {
            int defaultTicks = getDefaultDuration(type, data.isExtended());
            double seconds = defaultTicks / 20.0;
            System.out.println("  [DEBUG getCurrentDurationDouble] dari vanilla: " + seconds + " detik");
            return seconds;
        }
        
        return 0;
    }
    
    public static int getCurrentDuration(ItemStack item) {
        return (int) Math.round(getCurrentDurationDouble(item));
    }
    
    private static int getDefaultDuration(PotionType type, boolean extended) {
        String typeName = type.getKey().getKey().toLowerCase();
        
        if (typeName.contains("poison") || typeName.contains("weakness") || typeName.contains("slowness")) {
            return extended ? 1800 : 900;
        }
        if (typeName.contains("strength") || typeName.contains("jump") || 
            typeName.contains("regeneration") || typeName.contains("speed") ||
            typeName.contains("fire_resistance") || typeName.contains("invisibility") ||
            typeName.contains("night_vision") || typeName.contains("swiftness")) {
            return extended ? 9600 : 4800;
        }
        if (typeName.contains("turtle_master")) {
            return extended ? 800 : 400;
        }
        if (typeName.contains("slow_falling")) {
            return extended ? 3600 : 1800;
        }
        if (typeName.contains("water_breathing")) {
            return extended ? 4800 : 2400;
        }
        return extended ? 1800 : 900;
    }
    
    private static PotionEffectType getEffectTypeFromPotionType(String typeName) {
        switch (typeName) {
            case "speed":
            case "swiftness":
                return PotionEffectType.SPEED;
            case "slowness":
                return PotionEffectType.SLOWNESS;
            case "strength":
                return PotionEffectType.STRENGTH;
            case "jump_boost":
            case "leaping":
                return PotionEffectType.JUMP_BOOST;
            case "regeneration":
                return PotionEffectType.REGENERATION;
            case "fire_resistance":
                return PotionEffectType.FIRE_RESISTANCE;
            case "water_breathing":
                return PotionEffectType.WATER_BREATHING;
            case "invisibility":
                return PotionEffectType.INVISIBILITY;
            case "night_vision":
                return PotionEffectType.NIGHT_VISION;
            case "weakness":
                return PotionEffectType.WEAKNESS;
            case "poison":
                return PotionEffectType.POISON;
            case "slow_falling":
                return PotionEffectType.SLOW_FALLING;
            case "turtle_master":
                return PotionEffectType.RESISTANCE;
            default:
                System.out.println("  [ERROR] Unknown potion type: " + typeName);
                return null;
        }
    }
    
    private static String getPotionDisplayName(String baseType, int level) {
        String displayName;
        
        switch (baseType) {
            case "speed":
            case "swiftness":
                displayName = "Potion of Speed";
                break;
            case "slowness":
                displayName = "Potion of Slowness";
                break;
            case "strength":
                displayName = "Potion of Strength";
                break;
            case "jump_boost":
            case "leaping":
                displayName = "Potion of Leaping";
                break;
            case "regeneration":
                displayName = "Potion of Regeneration";
                break;
            case "fire_resistance":
                displayName = "Potion of Fire Resistance";
                break;
            case "water_breathing":
                displayName = "Potion of Water Breathing";
                break;
            case "invisibility":
                displayName = "Potion of Invisibility";
                break;
            case "night_vision":
                displayName = "Potion of Night Vision";
                break;
            case "weakness":
                displayName = "Potion of Weakness";
                break;
            case "poison":
                displayName = "Potion of Poison";
                break;
            case "slow_falling":
                displayName = "Potion of Slow Falling";
                break;
            case "turtle_master":
                displayName = "Potion of the Turtle Master";
                break;
            default:
                displayName = "Potion";
        }
        
        if (level > 1) {
            displayName += " " + toRomanNumerals(level);
        }
        
        return displayName;
    }
    
    // Konversi angka ke romawi yang benar
    private static String toRomanNumerals(int number) {
        if (number <= 0) return "";
        
        // Hanya support sampai 10 dulu, kalau lebih pake angka biasa
        if (number > 10) return String.valueOf(number);
        
        String[] roman = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return roman[number];
    }
    
    // Method getAmplifierFromLevel - INI YANG DICARI
    private static int getAmplifierFromLevel(int level) {
        return level - 1;
    }
    
    private static String formatDuration(double seconds) {
        int totalSeconds = (int) Math.round(seconds);
        int minutes = totalSeconds / 60;
        int secs = totalSeconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }
    
    private static String getEffectDisplayName(PotionEffectType effectType) {
        if (effectType.equals(PotionEffectType.SPEED)) return "Speed";
        if (effectType.equals(PotionEffectType.SLOWNESS)) return "Slowness";
        if (effectType.equals(PotionEffectType.STRENGTH)) return "Strength";
        if (effectType.equals(PotionEffectType.JUMP_BOOST)) return "Jump Boost";
        if (effectType.equals(PotionEffectType.REGENERATION)) return "Regeneration";
        if (effectType.equals(PotionEffectType.FIRE_RESISTANCE)) return "Fire Resistance";
        if (effectType.equals(PotionEffectType.WATER_BREATHING)) return "Water Breathing";
        if (effectType.equals(PotionEffectType.INVISIBILITY)) return "Invisibility";
        if (effectType.equals(PotionEffectType.NIGHT_VISION)) return "Night Vision";
        if (effectType.equals(PotionEffectType.WEAKNESS)) return "Weakness";
        if (effectType.equals(PotionEffectType.POISON)) return "Poison";
        if (effectType.equals(PotionEffectType.SLOW_FALLING)) return "Slow Falling";
        if (effectType.equals(PotionEffectType.RESISTANCE)) return "Resistance";
        return effectType.getName();
    }
    
    public static void applyPotionEffects(ItemStack item, int level, double durationSeconds, String baseType) {
        if (!(item.getItemMeta() instanceof PotionMeta)) return;
        
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        
        PersistentDataContainer oldContainer = meta.getPersistentDataContainer();
        Integer oldLevel = oldContainer.get(LEVEL_KEY, PersistentDataType.INTEGER);
        Double oldDuration = oldContainer.get(DURATION_KEY, PersistentDataType.DOUBLE);
        String oldBaseType = oldContainer.get(BASE_TYPE_KEY, PersistentDataType.STRING);
        Boolean oldUpgraded = oldContainer.get(UPGRADED_KEY, PersistentDataType.BOOLEAN);
        Integer oldPotionType = oldContainer.get(POTION_TYPE_KEY, PersistentDataType.INTEGER);
        
        PotionEffectType effectType = getEffectTypeFromPotionType(baseType);
        if (effectType == null) {
            System.out.println("  [ERROR] effectType NULL untuk baseType: " + baseType);
            return;
        }
        
        System.out.println("  [DEBUG] Applying effect: " + effectType.getName() + " untuk baseType: " + baseType);
        
        int amplifier = getAmplifierFromLevel(level);
        int durationTicks = (int) Math.max(20, Math.round(durationSeconds * 20));
        
        meta.clearCustomEffects();
        
        PotionType water = PotionType.WATER;
        meta.setBasePotionData(new PotionData(water, false, false));
        
        meta.addCustomEffect(new PotionEffect(effectType, durationTicks, amplifier), true);
        
        if (baseType.equals("turtle_master")) {
            meta.addCustomEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, amplifier), true);
        }
        
        PersistentDataContainer newContainer = meta.getPersistentDataContainer();
        if (oldLevel != null) newContainer.set(LEVEL_KEY, PersistentDataType.INTEGER, oldLevel);
        if (oldDuration != null) newContainer.set(DURATION_KEY, PersistentDataType.DOUBLE, oldDuration);
        if (oldBaseType != null) newContainer.set(BASE_TYPE_KEY, PersistentDataType.STRING, oldBaseType);
        if (oldUpgraded != null) newContainer.set(UPGRADED_KEY, PersistentDataType.BOOLEAN, oldUpgraded);
        if (oldPotionType != null) newContainer.set(POTION_TYPE_KEY, PersistentDataType.INTEGER, oldPotionType);
        
        meta.setDisplayName(ChatColor.RESET + getPotionDisplayName(baseType, level));
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Level: " + ChatColor.WHITE + level);
        lore.add(ChatColor.GRAY + "Duration: " + ChatColor.WHITE + Math.round(durationSeconds) + "s");
        
        String effectName = getEffectDisplayName(effectType);
        lore.add(ChatColor.BLUE + effectName + " " + level + " (" + formatDuration(durationSeconds) + ")");
        
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        
        System.out.println("  [DEBUG] Applied effect - ticks: " + durationTicks + ", seconds (real): " + durationSeconds);
    }
    
    public static String getBasePotionType(ItemStack item) {
        if (!(item.getItemMeta() instanceof PotionMeta)) return "";
        
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        String baseType = container.get(BASE_TYPE_KEY, PersistentDataType.STRING);
        if (baseType != null) {
            return baseType;
        }
        
        PotionData data = meta.getBasePotionData();
        return data.getType().getKey().getKey();
    }
    
    public static void savePotionData(ItemStack item, int level, double durationSeconds, String baseType) {
        if (!(item.getItemMeta() instanceof PotionMeta)) return;
        
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        container.set(LEVEL_KEY, PersistentDataType.INTEGER, level);
        container.set(DURATION_KEY, PersistentDataType.DOUBLE, durationSeconds);
        container.set(BASE_TYPE_KEY, PersistentDataType.STRING, baseType);
        container.set(UPGRADED_KEY, PersistentDataType.BOOLEAN, true);
        container.set(POTION_TYPE_KEY, PersistentDataType.INTEGER, TYPE_NORMAL);
        
        item.setItemMeta(meta);
        
        applyPotionEffects(item, level, durationSeconds, baseType);
        
        PotionMeta verifyMeta = (PotionMeta) item.getItemMeta();
        Integer savedLevel = verifyMeta.getPersistentDataContainer().get(LEVEL_KEY, PersistentDataType.INTEGER);
        Double savedDuration = verifyMeta.getPersistentDataContainer().get(DURATION_KEY, PersistentDataType.DOUBLE);
        System.out.println("  [DEBUG savePotionData] Verified saved level: " + savedLevel + ", duration: " + savedDuration + "s");
    }
    
    public static void initializeNewPotion(ItemStack item) {
        if (!(item.getItemMeta() instanceof PotionMeta)) return;
        
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        PotionData data = meta.getBasePotionData();
        
        int level = data.isUpgraded() ? 2 : 1;
        String baseType = data.getType().getKey().getKey();
        
        double duration = 0;
        if (hasDuration(data.getType())) {
            int ticks = getDefaultDuration(data.getType(), data.isExtended());
            duration = ticks / 20.0;
        }
        
        System.out.println("  [DEBUG initializeNewPotion] Creating new potion - Level: " + level + ", Duration: " + duration + "s, Type: " + baseType);
        
        savePotionData(item, level, duration, baseType);
    }
    
    public static boolean hasPersistentData(ItemStack item) {
        if (!(item.getItemMeta() instanceof PotionMeta)) return false;
    
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
    
        boolean hasLevel = container.has(LEVEL_KEY, PersistentDataType.INTEGER);
        System.out.println("  [DEBUG hasPersistentData] hasLevel: " + hasLevel);
        return hasLevel;
    }
    
    public static int getPotionType(ItemStack item) {
        if (!(item.getItemMeta() instanceof PotionMeta)) return TYPE_NORMAL;
        
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        Integer type = container.get(POTION_TYPE_KEY, PersistentDataType.INTEGER);
        return type != null ? type : TYPE_NORMAL;
    }
    
    public static void setPotionType(ItemStack item, int type) {
        if (!(item.getItemMeta() instanceof PotionMeta)) return;
        
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        container.set(POTION_TYPE_KEY, PersistentDataType.INTEGER, type);
        item.setItemMeta(meta);
    }
}