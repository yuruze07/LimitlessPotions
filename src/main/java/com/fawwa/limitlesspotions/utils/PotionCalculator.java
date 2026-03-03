package com.fawwa.limitlesspotions.utils;

import com.fawwa.limitlesspotions.LimitlessPotions;
import com.fawwa.limitlesspotions.config.ConfigManager;

public class PotionCalculator {

  public static double calculateNewDurationForLevelUp(double currentDuration) {
    // RUMUS BARU: durasi_baru = durasi_sekarang * 40%
    return Math.max(0.1, currentDuration * 0.4);
  }

  public static double calculateDurationBonus(
    int currentLevel,
    double currentDuration
  ) {
    // RUMUS BARU: tambahan_durasi = 10 * (40%)^(level_potions)
    // 40% = 0.4

    double bonus = 10 * Math.pow(0.4, currentLevel - 1);

    // Minimal 0.1 detik
    bonus = Math.max(0.1, bonus);

    // Debug
    System.out.println(
      "  [DEBUG] Level: " +
        currentLevel +
        ", Bonus: " +
        String.format("%.2f", bonus) +
        "s"
    );

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
