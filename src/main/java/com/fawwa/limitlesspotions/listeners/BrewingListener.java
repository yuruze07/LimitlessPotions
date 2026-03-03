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
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

public class BrewingListener implements Listener {

  private ItemStack lastFuel = null;
  private BrewerInventory lastInventory = null;

  @EventHandler
  public void onBrew(BrewEvent event) {
    BrewerInventory inventory = event.getContents();
    ItemStack ingredient = inventory.getIngredient();

    if (ingredient == null) return;

    // ===== SIMPAN FUEL LEVEL SEBELUM BREWING =====
    org.bukkit.block.BrewingStand brewingStand = inventory.getHolder();
    int fuelLevelBefore = brewingStand.getFuelLevel();

    // ===== CEK APAKAH ADA CUSTOM POTION =====
    boolean hasCustomPotion = false;
    for (int i = 0; i < 3; i++) {
      ItemStack bottle = inventory.getItem(i);
      if (bottle != null && PotionValidator.hasPersistentData(bottle)) {
        hasCustomPotion = true;
        break;
      }
    }

    // ===== JIKA ADA CUSTOM POTION, BLOKIR INGREDIENT YANG TIDAK DIIZINKAN =====
    if (hasCustomPotion) {
      Material ingredientType = ingredient.getType();
      boolean isAllowed =
        ingredientType == Material.GLOWSTONE_DUST ||
        ingredientType == Material.REDSTONE ||
        ingredientType == Material.GUNPOWDER ||
        ingredientType == Material.DRAGON_BREATH;

      if (!isAllowed) {
        event.setCancelled(true);

        // ===== KEMBALIKAN FUEL KE NILAI AWAL =====
        int finalFuelLevel = fuelLevelBefore;
        LimitlessPotions.getInstance()
          .getServer()
          .getScheduler()
          .runTaskLater(
            LimitlessPotions.getInstance(),
            () -> {
              brewingStand.setFuelLevel(finalFuelLevel);
            },
            1L
          );
        return;
      }
    }

    // ===== SISANYA LOGIC YANG SUDAH ADA =====
    LimitlessPotions.getInstance()
      .getServer()
      .getScheduler()
      .runTaskLater(
        LimitlessPotions.getInstance(),
        () -> replaceBrewedPotions(event.getContents()),
        1L
      );
  }

  @EventHandler
  public void onFuel(BrewingStandFuelEvent event) {
    // Empty
  }

  private void replaceBrewedPotions(BrewerInventory inventory) {
    for (int i = 0; i < 3; i++) {
      ItemStack bottle = inventory.getItem(i);
      if (bottle == null) continue;

      // Cek apakah ini potion
      boolean isValidPotion =
        bottle.getType() == Material.POTION ||
        bottle.getType() == Material.SPLASH_POTION ||
        bottle.getType() == Material.LINGERING_POTION;

      if (!isValidPotion) continue;

      // Skip jika sudah custom
      if (PotionValidator.hasPersistentData(bottle)) continue;

      // Skip base potions (water, awkward, mundane, thick)
      if (isBasePotion(bottle)) continue;

      // Convert ke custom potion
      convertToCustomPotion(bottle);
    }
  }

  private boolean isBasePotion(ItemStack bottle) {
    if (!(bottle.getItemMeta() instanceof PotionMeta)) return true;

    PotionMeta meta = (PotionMeta) bottle.getItemMeta();
    PotionData data = meta.getBasePotionData();
    PotionType type = data.getType();
    String typeName = type.getKey().getKey().toLowerCase();

    return (
      typeName.equals("water") ||
      typeName.equals("awkward") ||
      typeName.equals("mundane") ||
      typeName.equals("thick")
    );
  }

  private void convertToCustomPotion(ItemStack bottle) {
    if (!(bottle.getItemMeta() instanceof PotionMeta)) return;

    PotionMeta meta = (PotionMeta) bottle.getItemMeta();
    PotionData data = meta.getBasePotionData();

    String baseType = data.getType().getKey().getKey().toLowerCase();
    int level = data.isUpgraded() ? 2 : 1;

    // Ambil durasi dari mapping manual
    int ticks = getVanillaDurationTicks(data.getType(), data.isExtended());
    double duration = ticks / 20.0;

    // Save sebagai custom potion
    PotionValidator.savePotionData(bottle, level, duration, baseType);
  }

  private int getVanillaDurationTicks(PotionType type, boolean extended) {
    String typeName = type.getKey().getKey().toLowerCase();

    // ===== GROUP 1: 3 MENIT / 8 MENIT (3600 / 9600 ticks) =====
    // Semua potion dengan durasi dasar 3 menit, extended 8 menit
    if (
      typeName.contains("speed") ||
      typeName.contains("swiftness") ||
      typeName.contains("strength") ||
      typeName.contains("jump") ||
      typeName.contains("leaping") ||
      typeName.contains("fire_resistance") ||
      typeName.contains("water_breathing") ||
      typeName.contains("invisibility") ||
      typeName.contains("night_vision")
    ) {
      return extended ? 9600 : 4800; // 8:00 / 4:00
    }

    // ===== GROUP 2: 1:30 MENIT / 4 MENIT (1800 / 3600 ticks) =====
    // Slowness, Weakness
    if (typeName.contains("slowness") || typeName.contains("weakness")) {
      return extended ? 3600 : 1800; // 4:00 / 1:30
    }

    // ===== GROUP 3: 0:45 / 1:30 MENIT (900 / 1800 ticks) =====
    // Regeneration, Poison
    if (typeName.contains("regeneration") || typeName.contains("poison")) {
      return extended ? 1800 : 900; // 1:30 / 0:45
    }

    // ===== GROUP 4: 1:30 / 3 MENIT (1800 / 3600 ticks) =====
    // Slow Falling
    if (typeName.contains("slow_falling")) {
      return extended ? 3600 : 1800; // 3:00 / 1:30
    }

    // ===== GROUP 5: 0:20 / 0:40 MENIT (400 / 800 ticks) =====
    // Turtle Master
    if (typeName.contains("turtle_master")) {
      return extended ? 800 : 400; // 0:40 / 0:20
    }

    // ===== GROUP 6: INSTANT (1 tick) =====
    // Healing, Harming
    if (
      typeName.contains("healing") ||
      typeName.contains("instant_health") ||
      typeName.contains("harming") ||
      typeName.contains("instant_damage")
    ) {
      return 1; // Instant
    }

    // ===== GROUP 7: 1.21 POTIONS - 3 MENIT / 6 MENIT (3600 / 7200 ticks) =====
    // Infested, Oozing, Weaving, Wind Charged
    if (
      typeName.contains("infested") ||
      typeName.contains("oozing") ||
      typeName.contains("weaving") ||
      typeName.contains("wind_charged")
    ) {
      return extended ? 7200 : 3600; // 6:00 / 3:00
    }

    // Default fallback
    return extended ? 7200 : 3600; // 6:00 / 3:00
  }

  // Handler untuk upgrade (redstone/glowstone)
  @EventHandler
  public void onBrewUpgrade(BrewEvent event) {
    BrewerInventory inventory = event.getContents();
    ItemStack ingredient = inventory.getIngredient();

    if (ingredient == null) return;

    Material ingredientType = ingredient.getType();
    boolean isLevelUp = ingredientType == Material.GLOWSTONE_DUST;
    boolean isDurationUp = ingredientType == Material.REDSTONE;

    if (!isLevelUp && !isDurationUp) return;

    event.setCancelled(true);

    boolean success = false;

    for (int i = 0; i < 3; i++) {
      ItemStack bottle = inventory.getItem(i);
      if (bottle == null) continue;

      boolean isValidPotion =
        bottle.getType() == Material.POTION ||
        bottle.getType() == Material.SPLASH_POTION ||
        bottle.getType() == Material.LINGERING_POTION;

      if (!isValidPotion) continue;
      if (!PotionValidator.isValidPotion(bottle)) continue;

      // Pastikan punya persistent data
      if (!PotionValidator.hasPersistentData(bottle)) {
        PotionValidator.initializeNewPotion(bottle);
      }

      int currentLevel = PotionValidator.getCurrentLevel(bottle);
      double currentDuration = PotionValidator.getCurrentDurationDouble(bottle);
      String baseType = PotionValidator.getBasePotionType(bottle);

      if (isLevelUp) {
        if (!PotionCalculator.canUpgradeLevel(currentLevel + 1)) continue;

        double newDuration = PotionCalculator.calculateNewDurationForLevelUp(
          currentDuration
        );
        int newLevel = currentLevel + 1;

        PotionValidator.savePotionData(bottle, newLevel, newDuration, baseType);
        inventory.setItem(i, bottle);
        success = true;
      } else if (isDurationUp) {
        if (!PotionCalculator.canAddDuration(currentDuration)) continue;

        double bonus = PotionCalculator.calculateDurationBonus(
          currentLevel,
          currentDuration
        );
        double newDuration = currentDuration + bonus;

        PotionValidator.savePotionData(
          bottle,
          currentLevel,
          newDuration,
          baseType
        );
        inventory.setItem(i, bottle);
        success = true;
      }
    }

    if (success) {
      ingredient.setAmount(ingredient.getAmount() - 1);
      inventory.setIngredient(ingredient);
      inventory.getHolder().update();
    }
  }

  // Handler untuk gunpowder (splash)
  @EventHandler
  public void onBrewGunpowder(BrewEvent event) {
    BrewerInventory inventory = event.getContents();
    ItemStack ingredient = inventory.getIngredient();

    if (ingredient == null) return;
    if (ingredient.getType() != Material.GUNPOWDER) return;

    event.setCancelled(true);

    boolean success = false;

    for (int i = 0; i < 3; i++) {
      ItemStack bottle = inventory.getItem(i);
      if (bottle == null || bottle.getType() != Material.POTION) continue;

      if (!PotionValidator.isValidPotion(bottle)) continue;

      if (!PotionValidator.hasPersistentData(bottle)) {
        PotionValidator.initializeNewPotion(bottle);
      }

      int level = PotionValidator.getCurrentLevel(bottle);
      double duration = PotionValidator.getCurrentDurationDouble(bottle);
      String baseType = PotionValidator.getBasePotionType(bottle);

      ItemStack splashPotion = new ItemStack(Material.SPLASH_POTION, 1);
      PotionValidator.savePotionData(splashPotion, level, duration, baseType);
      inventory.setItem(i, splashPotion);
      success = true;
    }

    if (success) {
      ingredient.setAmount(ingredient.getAmount() - 1);
      inventory.setIngredient(ingredient);
      inventory.getHolder().update();
    }
  }

  // Handler untuk dragon breath (lingering)
  @EventHandler
  public void onBrewDragonBreath(BrewEvent event) {
    BrewerInventory inventory = event.getContents();
    ItemStack ingredient = inventory.getIngredient();

    if (ingredient == null) return;
    if (ingredient.getType() != Material.DRAGON_BREATH) return;

    event.setCancelled(true);

    boolean success = false;

    for (int i = 0; i < 3; i++) {
      ItemStack bottle = inventory.getItem(i);
      if (bottle == null) continue;

      boolean isValidSource =
        bottle.getType() == Material.POTION ||
        bottle.getType() == Material.SPLASH_POTION;

      if (!isValidSource) continue;
      if (!PotionValidator.isValidPotion(bottle)) continue;

      if (!PotionValidator.hasPersistentData(bottle)) {
        PotionValidator.initializeNewPotion(bottle);
      }

      int level = PotionValidator.getCurrentLevel(bottle);
      double duration = PotionValidator.getCurrentDurationDouble(bottle);
      String baseType = PotionValidator.getBasePotionType(bottle);

      ItemStack lingeringPotion = new ItemStack(Material.LINGERING_POTION, 1);
      PotionValidator.savePotionData(
        lingeringPotion,
        level,
        duration,
        baseType
      );
      inventory.setItem(i, lingeringPotion);
      success = true;
    }

    if (success) {
      ingredient.setAmount(ingredient.getAmount() - 1);
      inventory.setIngredient(ingredient);
      inventory.getHolder().update();
    }
  }
}
