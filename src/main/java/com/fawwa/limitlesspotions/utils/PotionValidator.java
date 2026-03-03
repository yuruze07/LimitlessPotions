package com.fawwa.limitlesspotions.utils;

import com.fawwa.limitlesspotions.LimitlessPotions;
import java.util.ArrayList;
import java.util.List;
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

public class PotionValidator {

  private static final NamespacedKey LEVEL_KEY = new NamespacedKey(
    LimitlessPotions.getInstance(),
    "potion_level"
  );
  private static final NamespacedKey DURATION_KEY = new NamespacedKey(
    LimitlessPotions.getInstance(),
    "potion_duration_double"
  );
  private static final NamespacedKey BASE_TYPE_KEY = new NamespacedKey(
    LimitlessPotions.getInstance(),
    "base_potion_type"
  );
  private static final NamespacedKey UPGRADED_KEY = new NamespacedKey(
    LimitlessPotions.getInstance(),
    "is_upgraded"
  );
  private static final NamespacedKey POTION_TYPE_KEY = new NamespacedKey(
    LimitlessPotions.getInstance(),
    "potion_type"
  );

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

    // Debug: lihat apa yang diterima
    // System.out.println(
    //   "  [DEBUG isValidPotion] Checking potion type: " + typeName
    // );

    // Mapping untuk 1.21 potions
    if (
      typeName.contains("infested") ||
      typeName.contains("oozing") ||
      typeName.contains("weaving") ||
      typeName.contains("wind_charged")
    ) {
      //   System.out.println(
      //     "  [DEBUG isValidPotion] Detected 1.21 potion: " + typeName
      //   );
      return true; // Anggap valid untuk 1.21 potions
    }

    return !LimitlessPotions.getInstance()
      .getConfigManager()
      .getExcludedPotions()
      .contains(typeName);
  }

  public static boolean isInstantPotion(PotionType type) {
    String typeName = type.getKey().getKey().toLowerCase();
    return (
      typeName.contains("instant") ||
      typeName.equals("healing") ||
      typeName.equals("harming") ||
      typeName.equals("strong_healing") ||
      typeName.equals("strong_harming")
    );
  }

  public static boolean hasDuration(PotionType type) {
    String typeName = type.getKey().getKey().toLowerCase();
    return !(
      typeName.contains("healing") ||
      typeName.contains("harming") ||
      typeName.contains("instant")
    );
  }

  public static boolean isCustomPotion(ItemStack item) {
    if (!(item.getItemMeta() instanceof PotionMeta)) return false;

    PotionMeta meta = (PotionMeta) item.getItemMeta();
    PersistentDataContainer container = meta.getPersistentDataContainer();

    boolean hasLevel = container.has(LEVEL_KEY, PersistentDataType.INTEGER);
    boolean hasUpgraded = container.has(
      UPGRADED_KEY,
      PersistentDataType.BOOLEAN
    );

    // CEK APAKAH INI 1.21 POTION
    PotionData data = meta.getBasePotionData();
    String typeName = data.getType().getKey().getKey().toLowerCase();
    boolean isNewPotion =
      typeName.contains("infested") ||
      typeName.contains("oozing") ||
      typeName.contains("weaving") ||
      typeName.contains("wind_charged");

    // System.out.println(
    //   "  [DEBUG isCustomPotion] hasLevel: " +
    //     hasLevel +
    //     ", hasUpgraded: " +
    //     hasUpgraded +
    //     ", isNewPotion: " +
    //     isNewPotion +
    //     ", typeName: " +
    //     typeName
    // );

    // ANGGAP SEBAGAI CUSTOM POTION JIKA:
    // 1. Punya persistent data ATAU
    // 2. Ini adalah 1.21 potion (infested, oozing, weaving, wind_charged)
    return hasLevel || hasUpgraded || isNewPotion;
  }

  public static int getCurrentLevel(ItemStack item) {
    if (!(item.getItemMeta() instanceof PotionMeta)) return 1;

    PotionMeta meta = (PotionMeta) item.getItemMeta();
    PersistentDataContainer container = meta.getPersistentDataContainer();

    Integer level = container.get(LEVEL_KEY, PersistentDataType.INTEGER);
    if (level != null) {
      //   System.out.println("  [DEBUG getCurrentLevel] dari persistent: " + level);
      return level;
    }

    PotionData data = meta.getBasePotionData();
    if (data.isUpgraded()) {
      //   System.out.println("  [DEBUG getCurrentLevel] dari vanilla upgraded: 2");
      return 2;
    }

    // System.out.println("  [DEBUG getCurrentLevel] default: 1");
    return 1;
  }

  public static double getCurrentDurationDouble(ItemStack item) {
    if (!(item.getItemMeta() instanceof PotionMeta)) return 0;

    PotionMeta meta = (PotionMeta) item.getItemMeta();
    PersistentDataContainer container = meta.getPersistentDataContainer();

    Double duration = container.get(DURATION_KEY, PersistentDataType.DOUBLE);
    if (duration != null) {
      //   System.out.println(
      //     "  [DEBUG getCurrentDurationDouble] dari persistent: " +
      //       duration +
      //       " detik (real)"
      //   );
      return duration;
    }

    PotionData data = meta.getBasePotionData();
    PotionType type = data.getType();

    if (hasDuration(type)) {
      int defaultTicks = getDefaultDuration(type, data.isExtended());
      double seconds = defaultTicks / 20.0;
      //   System.out.println(
      //     "  [DEBUG getCurrentDurationDouble] dari vanilla: " + seconds + " detik"
      //   );
      return seconds;
    }

    return 0;
  }

  public static int getCurrentDuration(ItemStack item) {
    return (int) Math.round(getCurrentDurationDouble(item));
  }

  private static int getDefaultDuration(PotionType type, boolean extended) {
    String typeName = type.getKey().getKey().toLowerCase();

    if (
      typeName.contains("poison") ||
      typeName.contains("weakness") ||
      typeName.contains("slowness")
    ) {
      return extended ? 1800 : 900;
    }
    if (
      typeName.contains("strength") ||
      typeName.contains("jump") ||
      typeName.contains("regeneration") ||
      typeName.contains("speed") ||
      typeName.contains("fire_resistance") ||
      typeName.contains("invisibility") ||
      typeName.contains("night_vision") ||
      typeName.contains("swiftness")
    ) {
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
    if (
      typeName.contains("infested") ||
      typeName.contains("oozing") ||
      typeName.contains("weaving") ||
      typeName.contains("wind_charged")
    ) {
      return 1800; // 1.5 menit default untuk 1.21 potions
    }
    return extended ? 1800 : 900;
  }

  private static PotionEffectType getEffectTypeFromPotionType(String typeName) {
    // Normalisasi input
    String normalized = typeName.toLowerCase().trim();
    // System.out.println(
    //   "  [DEBUG getEffectType] Looking for: '" + normalized + "'"
    // );

    // Mapping untuk 1.21 potions - TARUH DI PALING ATAS
    if (normalized.equals("infested") || normalized.contains("infested")) {
      //   System.out.println("  [DEBUG getEffectType] Mapping ke INFESTED");
      return PotionEffectType.INFESTED;
    }
    if (normalized.equals("oozing") || normalized.contains("oozing")) {
      //   System.out.println("  [DEBUG getEffectType] Mapping ke OOZING");
      return PotionEffectType.OOZING;
    }
    if (normalized.equals("weaving") || normalized.contains("weaving")) {
      //   System.out.println("  [DEBUG getEffectType] Mapping ke WEAVING");
      return PotionEffectType.WEAVING;
    }
    if (
      normalized.equals("wind_charged") ||
      normalized.contains("wind_charged") ||
      normalized.contains("wind")
    ) {
      //   System.out.println("  [DEBUG getEffectType] Mapping ke WIND_CHARGED");
      return PotionEffectType.WIND_CHARGED;
    }

    // JANGAN inisialisasi water/awkward/mundane/thick
    if (
      normalized.equals("water") ||
      normalized.equals("awkward") ||
      normalized.equals("mundane") ||
      normalized.equals("thick")
    ) {
      //   System.out.println(
      //     "  [DEBUG getEffectType] Base potion type (no effect): " + normalized
      //   );
      return null; // Base potions don't have effects
    }

    // Mapping untuk potion biasa
    switch (normalized) {
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
      case "healing":
      case "instant_health":
        return PotionEffectType.INSTANT_HEALTH;
      case "harming":
      case "instant_damage":
        return PotionEffectType.INSTANT_DAMAGE;
      default:
        // System.out.println("  [ERROR] Unknown potion type: " + typeName);
        return null;
    }
  }

  private static String getPotionDisplayName(String baseType, int level) {
    String displayName;

    switch (baseType) {
      // 🍶 Potions dengan Efek Positif (Buff)
      case "speed":
      case "swiftness":
        displayName = "Potion of Swiftness";
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
      // ⚠️ Potions dengan Efek Negatif (Debuff)
      case "healing":
      case "instant_health":
        displayName = "Potion of Healing";
        break;
      case "harming":
      case "instant_damage":
        displayName = "Potion of Harming";
        break;
      // 🆕 Efek yang terlewat
      case "haste":
        displayName = "Potion of Haste";
        break;
      case "mining_fatigue":
        displayName = "Potion of Mining Fatigue";
        break;
      case "nausea":
        displayName = "Potion of Nausea";
        break;
      case "wither":
        displayName = "Potion of Wither";
        break;
      case "health_boost":
        displayName = "Potion of Health Boost";
        break;
      case "absorption":
        displayName = "Potion of Absorption";
        break;
      case "saturation":
        displayName = "Potion of Saturation";
        break;
      case "glowing":
        displayName = "Potion of Glowing";
        break;
      case "levitation":
        displayName = "Potion of Levitation";
        break;
      case "luck":
        displayName = "Potion of Luck";
        break;
      case "unluck":
        displayName = "Potion of Unluck";
        break;
      case "conduit_power":
        displayName = "Potion of Conduit Power";
        break;
      case "dolphins_grace":
        displayName = "Potion of Dolphin's Grace";
        break;
      case "bad_omen":
        displayName = "Potion of Bad Omen";
        break;
      case "raid_omen":
        displayName = "Potion of Raid Omen";
        break;
      case "trial_omen":
        displayName = "Potion of Trial Omen";
        break;
      case "hero_of_the_village":
        displayName = "Potion of Hero of the Village";
        break;
      case "darkness":
        displayName = "Potion of Darkness";
        break;
      // 🐉 Mob-Effect Potions (1.21)
      case "infested":
        displayName = "Potion of Infestation";
        break;
      case "oozing":
        displayName = "Potion of Oozing";
        break;
      case "weaving":
        displayName = "Potion of Weaving";
        break;
      case "wind_charged":
        displayName = "Potion of Wind Charging";
        break;
      default:
        displayName = "Potion";
    }

    if (level > 1) {
      displayName += " " + toRomanNumerals(level);
    }

    return displayName;
  }

  // Konversi angka ke romawi
  private static String toRomanNumerals(int number) {
    if (number <= 0) return "";
    if (number > 10) return String.valueOf(number);
    String[] roman = {
      "",
      "I",
      "II",
      "III",
      "IV",
      "V",
      "VI",
      "VII",
      "VIII",
      "IX",
      "X",
    };
    return roman[number];
  }

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
    if (effectType == null) return "Unknown";

    // Mapping lengkap display names
    if (effectType.equals(PotionEffectType.SPEED)) return "Speed";
    if (effectType.equals(PotionEffectType.SLOWNESS)) return "Slowness";
    if (effectType.equals(PotionEffectType.HASTE)) return "Haste";
    if (
      effectType.equals(PotionEffectType.MINING_FATIGUE)
    ) return "Mining Fatigue";
    if (effectType.equals(PotionEffectType.STRENGTH)) return "Strength";
    if (
      effectType.equals(PotionEffectType.INSTANT_HEALTH)
    ) return "Instant Health";
    if (
      effectType.equals(PotionEffectType.INSTANT_DAMAGE)
    ) return "Instant Damage";
    if (effectType.equals(PotionEffectType.JUMP_BOOST)) return "Jump Boost";
    if (effectType.equals(PotionEffectType.NAUSEA)) return "Nausea";
    if (effectType.equals(PotionEffectType.REGENERATION)) return "Regeneration";
    if (effectType.equals(PotionEffectType.RESISTANCE)) return "Resistance";
    if (
      effectType.equals(PotionEffectType.FIRE_RESISTANCE)
    ) return "Fire Resistance";
    if (
      effectType.equals(PotionEffectType.WATER_BREATHING)
    ) return "Water Breathing";
    if (effectType.equals(PotionEffectType.INVISIBILITY)) return "Invisibility";
    if (effectType.equals(PotionEffectType.BLINDNESS)) return "Blindness";
    if (effectType.equals(PotionEffectType.NIGHT_VISION)) return "Night Vision";
    if (effectType.equals(PotionEffectType.HUNGER)) return "Hunger";
    if (effectType.equals(PotionEffectType.WEAKNESS)) return "Weakness";
    if (effectType.equals(PotionEffectType.POISON)) return "Poison";
    if (effectType.equals(PotionEffectType.WITHER)) return "Wither";
    if (effectType.equals(PotionEffectType.HEALTH_BOOST)) return "Health Boost";
    if (effectType.equals(PotionEffectType.ABSORPTION)) return "Absorption";
    if (effectType.equals(PotionEffectType.SATURATION)) return "Saturation";
    if (effectType.equals(PotionEffectType.GLOWING)) return "Glowing";
    if (effectType.equals(PotionEffectType.LEVITATION)) return "Levitation";
    if (effectType.equals(PotionEffectType.LUCK)) return "Luck";
    if (effectType.equals(PotionEffectType.UNLUCK)) return "Unluck";
    if (effectType.equals(PotionEffectType.SLOW_FALLING)) return "Slow Falling";
    if (
      effectType.equals(PotionEffectType.CONDUIT_POWER)
    ) return "Conduit Power";
    if (
      effectType.equals(PotionEffectType.DOLPHINS_GRACE)
    ) return "Dolphin's Grace";
    if (effectType.equals(PotionEffectType.BAD_OMEN)) return "Bad Omen";
    if (effectType.equals(PotionEffectType.RAID_OMEN)) return "Raid Omen";
    if (effectType.equals(PotionEffectType.TRIAL_OMEN)) return "Trial Omen";
    if (
      effectType.equals(PotionEffectType.HERO_OF_THE_VILLAGE)
    ) return "Hero of the Village";
    if (effectType.equals(PotionEffectType.DARKNESS)) return "Darkness";
    if (effectType.equals(PotionEffectType.INFESTED)) return "Infested";
    if (effectType.equals(PotionEffectType.OOZING)) return "Oozing";
    if (effectType.equals(PotionEffectType.WEAVING)) return "Weaving";
    if (effectType.equals(PotionEffectType.WIND_CHARGED)) return "Wind Charged";

    return effectType.getName();
  }

  public static void applyPotionEffects(
    ItemStack item,
    int level,
    double durationSeconds,
    String baseType
  ) {
    if (!(item.getItemMeta() instanceof PotionMeta)) return;

    PotionMeta meta = (PotionMeta) item.getItemMeta();

    PersistentDataContainer oldContainer = meta.getPersistentDataContainer();
    Integer oldLevel = oldContainer.get(LEVEL_KEY, PersistentDataType.INTEGER);
    Double oldDuration = oldContainer.get(
      DURATION_KEY,
      PersistentDataType.DOUBLE
    );
    String oldBaseType = oldContainer.get(
      BASE_TYPE_KEY,
      PersistentDataType.STRING
    );
    Boolean oldUpgraded = oldContainer.get(
      UPGRADED_KEY,
      PersistentDataType.BOOLEAN
    );
    Integer oldPotionType = oldContainer.get(
      POTION_TYPE_KEY,
      PersistentDataType.INTEGER
    );

    PotionEffectType effectType = getEffectTypeFromPotionType(baseType);
    if (effectType == null) {
      //   System.out.println(
      //     "  [ERROR] effectType NULL untuk baseType: " + baseType
      //   );
      return;
    }

    // System.out.println(
    //   "  [DEBUG] Applying effect: " +
    //     effectType.getName() +
    //     " untuk baseType: " +
    //     baseType
    // );

    int amplifier = getAmplifierFromLevel(level);
    int durationTicks = (int) Math.max(20, Math.round(durationSeconds * 20));

    // ===== PERBAIKAN: JANGAN HAPUS EFFECT YANG ADA =====
    // HAPUS BARIS INI: meta.clearCustomEffects();

    PotionType water = PotionType.WATER;
    meta.setBasePotionData(new PotionData(water, false, false));

    // Untuk instant potions, durationTicks harus 1
    if (
      effectType.equals(PotionEffectType.INSTANT_HEALTH) ||
      effectType.equals(PotionEffectType.INSTANT_DAMAGE)
    ) {
      durationTicks = 1;
    }

    // ===== CEK APAKAH EFEK SUDAH ADA =====
    boolean hasMainEffect = false;
    for (PotionEffect existing : meta.getCustomEffects()) {
      if (existing.getType().equals(effectType)) {
        hasMainEffect = true;
        // System.out.println(
        //   "  [DEBUG] Effect " + effectType.getName() + " sudah ada, skip"
        // );
        break;
      }
    }

    // Tambah efek utama hanya jika belum ada
    if (!hasMainEffect) {
      //   System.out.println("  [DEBUG] Menambah effect: " + effectType.getName());
      meta.addCustomEffect(
        new PotionEffect(effectType, durationTicks, amplifier),
        true
      );
    }

    // Special case untuk turtle_master yang punya 2 efek
    if (baseType.equals("turtle_master")) {
      boolean hasSlowness = false;
      for (PotionEffect existing : meta.getCustomEffects()) {
        if (existing.getType().equals(PotionEffectType.SLOWNESS)) {
          hasSlowness = true;
          //   System.out.println("  [DEBUG] Slowness sudah ada, skip");
          break;
        }
      }

      if (!hasSlowness) {
        // System.out.println("  [DEBUG] Menambah Slowness untuk turtle_master");
        meta.addCustomEffect(
          new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, amplifier),
          true
        );
      }
    }

    // Restore persistent data
    PersistentDataContainer newContainer = meta.getPersistentDataContainer();
    if (oldLevel != null) newContainer.set(
      LEVEL_KEY,
      PersistentDataType.INTEGER,
      oldLevel
    );
    if (oldDuration != null) newContainer.set(
      DURATION_KEY,
      PersistentDataType.DOUBLE,
      oldDuration
    );
    if (oldBaseType != null) newContainer.set(
      BASE_TYPE_KEY,
      PersistentDataType.STRING,
      oldBaseType
    );
    if (oldUpgraded != null) newContainer.set(
      UPGRADED_KEY,
      PersistentDataType.BOOLEAN,
      oldUpgraded
    );
    if (oldPotionType != null) newContainer.set(
      POTION_TYPE_KEY,
      PersistentDataType.INTEGER,
      oldPotionType
    );

    meta.setDisplayName(
      ChatColor.RESET + getPotionDisplayName(baseType, level)
    );

    List<String> lore = new ArrayList<>();
    lore.add(ChatColor.GRAY + "Level: " + ChatColor.WHITE + level);

    // Untuk instant potions, jangan tampilkan durasi
    if (
      effectType.equals(PotionEffectType.INSTANT_HEALTH) ||
      effectType.equals(PotionEffectType.INSTANT_DAMAGE)
    ) {
      lore.add(ChatColor.GRAY + "Type: " + ChatColor.WHITE + "Instant");
    } else {
      lore.add(
        ChatColor.GRAY +
          "Duration: " +
          ChatColor.WHITE +
          Math.round(durationSeconds) +
          "s"
      );
    }

    String effectName = getEffectDisplayName(effectType);

    if (
      effectType.equals(PotionEffectType.INSTANT_HEALTH) ||
      effectType.equals(PotionEffectType.INSTANT_DAMAGE)
    ) {
      lore.add(ChatColor.BLUE + effectName + " " + level);
    } else {
      lore.add(
        ChatColor.BLUE +
          effectName +
          " " +
          level +
          " (" +
          formatDuration(durationSeconds) +
          ")"
      );
    }

    // Untuk turtle_master, tambahkan slowness di lore
    if (baseType.equals("turtle_master")) {
      if (
        effectType.equals(PotionEffectType.INSTANT_HEALTH) ||
        effectType.equals(PotionEffectType.INSTANT_DAMAGE)
      ) {
        lore.add(ChatColor.BLUE + "Slowness " + level);
      } else {
        lore.add(
          ChatColor.BLUE +
            "Slowness " +
            level +
            " (" +
            formatDuration(durationSeconds) +
            ")"
        );
      }
    }

    meta.setLore(lore);
    item.setItemMeta(meta);

    // System.out.println(
    //   "  [DEBUG] Applied effect - ticks: " +
    //     durationTicks +
    //     ", seconds (real): " +
    //     durationSeconds
    // );
  }

  public static boolean isPotionTypeSupported(String typeName) {
    // Base potions (no effects)
    if (
      typeName.equals("water") ||
      typeName.equals("awkward") ||
      typeName.equals("mundane") ||
      typeName.equals("thick")
    ) {
      return true; // They are supported but have no effects
    }

    // Daftar semua tipe potion yang didukung
    String[] supportedTypes = {
      // Positif
      "speed",
      "swiftness",
      "slowness",
      "strength",
      "jump_boost",
      "leaping",
      "regeneration",
      "fire_resistance",
      "water_breathing",
      "invisibility",
      "night_vision",
      "weakness",
      "poison",
      "slow_falling",
      "turtle_master",
      // Negatif
      "instant_health",
      "healing",
      "instant_damage",
      "harming",
      // 1.21
      "infested",
      "oozing",
      "weaving",
      "wind_charged",
      // Lainnya
      "haste",
      "mining_fatigue",
      "nausea",
      "wither",
      "health_boost",
      "absorption",
      "saturation",
      "glowing",
      "levitation",
      "luck",
      "unluck",
      "conduit_power",
      "dolphins_grace",
      "bad_omen",
      "raid_omen",
      "trial_omen",
      "hero_of_the_village",
      "darkness",
    };

    for (String supported : supportedTypes) {
      if (supported.equals(typeName)) {
        return true;
      }
    }
    return false;
  }

  public static String getBasePotionType(ItemStack item) {
    if (!(item.getItemMeta() instanceof PotionMeta)) return "";

    PotionMeta meta = (PotionMeta) item.getItemMeta();
    PersistentDataContainer container = meta.getPersistentDataContainer();

    // Coba ambil dari persistent data dulu
    String baseType = container.get(BASE_TYPE_KEY, PersistentDataType.STRING);
    if (baseType != null) {
      //   System.out.println(
      //     "  [DEBUG getBasePotionType] dari persistent: " + baseType
      //   );
      return baseType;
    }

    // Jika tidak ada, ambil dari PotionData
    PotionData data = meta.getBasePotionData();
    PotionType type = data.getType();
    String typeName = type.getKey().getKey().toLowerCase();

    // MAPPING KHUSUS: Untuk potion 1.21, nama yang dikembalikan mungkin perlu disesuaikan
    // System.out.println("  [DEBUG getBasePotionType] dari vanilla: " + typeName);

    // Mapping untuk potion 1.21 - TAMBAHKAN LEBIH BANYAK VARIASI
    if (typeName.contains("infest")) {
      // Akan match "infested", "infestation", dll
      return "infested";
    }
    if (typeName.contains("ooz")) {
      // Akan match "oozing"
      return "oozing";
    }
    if (typeName.contains("weav")) {
      // Akan match "weaving"
      return "weaving";
    }
    if (typeName.contains("wind")) {
      // Akan match "wind_charged"
      return "wind_charged";
    }

    return typeName;
  }

  public static void savePotionData(
    ItemStack item,
    int level,
    double durationSeconds,
    String baseType
  ) {
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
    Integer savedLevel = verifyMeta
      .getPersistentDataContainer()
      .get(LEVEL_KEY, PersistentDataType.INTEGER);
    Double savedDuration = verifyMeta
      .getPersistentDataContainer()
      .get(DURATION_KEY, PersistentDataType.DOUBLE);
    // System.out.println(
    //   "  [DEBUG savePotionData] Verified saved level: " +
    //     savedLevel +
    //     ", duration: " +
    //     savedDuration +
    //     "s"
    // );
  }

  public static void initializeNewPotion(ItemStack item) {
    if (!(item.getItemMeta() instanceof PotionMeta)) return;

    PotionMeta meta = (PotionMeta) item.getItemMeta();
    PotionData data = meta.getBasePotionData();

    // CEK JANGAN INISIALISASI BASE POTIONS
    PotionType type = data.getType();
    String typeName = type.getKey().getKey().toLowerCase();

    boolean isBasePotion =
      typeName.equals("water") ||
      typeName.equals("awkward") ||
      typeName.equals("mundane") ||
      typeName.equals("thick");

    if (isBasePotion) {
      //   System.out.println(
      //     "  [DEBUG initializeNewPotion] Cannot initialize base potion: " +
      //       typeName
      //   );
      return;
    }

    int level = data.isUpgraded() ? 2 : 1;
    String baseType = getBasePotionType(item);

    double duration = 0;
    if (hasDuration(data.getType())) {
      int ticks = getDefaultDuration(data.getType(), data.isExtended());
      duration = ticks / 20.0;
    }

    // System.out.println(
    //   "  [DEBUG initializeNewPotion] Creating new potion - Level: " +
    //     level +
    //     ", Duration: " +
    //     duration +
    //     "s, BaseType: " +
    //     baseType
    // );

    savePotionData(item, level, duration, baseType);
  }

  public static boolean hasPersistentData(ItemStack item) {
    if (!(item.getItemMeta() instanceof PotionMeta)) return false;

    PotionMeta meta = (PotionMeta) item.getItemMeta();
    PersistentDataContainer container = meta.getPersistentDataContainer();

    boolean hasLevel = container.has(LEVEL_KEY, PersistentDataType.INTEGER);
    boolean hasDuration = container.has(
      DURATION_KEY,
      PersistentDataType.DOUBLE
    );
    boolean hasBaseType = container.has(
      BASE_TYPE_KEY,
      PersistentDataType.STRING
    );
    boolean hasUpgraded = container.has(
      UPGRADED_KEY,
      PersistentDataType.BOOLEAN
    );

    // System.out.println(
    //   "  [DEBUG hasPersistentData] hasLevel: " +
    //     hasLevel +
    //     ", hasDuration: " +
    //     hasDuration +
    //     ", hasBaseType: " +
    //     hasBaseType +
    //     ", hasUpgraded: " +
    //     hasUpgraded
    // );

    // Return true jika setidaknya punya level atau upgraded
    return hasLevel || hasUpgraded;
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
