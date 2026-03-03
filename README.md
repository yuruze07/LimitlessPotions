# LimitlessPotions

A Minecraft Paper plugin that allows unlimited potion upgrading with configurable formulas.

⚠️ **WARNING**: Still in development, may contain bugs

## Features

- **Upgrade potion levels** with Glowstone Dust (new duration = current duration × 40%)
- **Extend potion duration** with Redstone (bonus = 10 × (0.4)^level)
- **Support for all brewable potions** including 1.21 potions (Infested, Oozing, Weaving, Wind Charged)
- **Instant health/damage potions** are excluded from duration upgrades
- **Unlimited upgrades** (configurable max level)
- **Custom potion lore** showing level and duration
- **Splash and lingering potion support**
- **Ingredient blocking** for custom potions (only redstone, glowstone, gunpowder, dragon breath allowed)

## How it works

When you brew a potion, it's automatically converted to a custom potion with persistent data. You can then:

- **Glowstone Dust**: Increases potion level, reduces duration by 40%
- **Redstone**: Adds bonus duration (diminishing returns based on level)
- **Gunpowder**: Converts to splash potion
- **Dragon Breath**: Converts to lingering potion

## Requirements

- Paper 1.21.11 or higher
- Java 21

## Building from source

```
mvn clean package
```
The JAR will be in target/LimitlessPotions-X.X.X.jar

## Known Issues
- Fuel restoration when blocking ingredients doesn't work properly (fuel still decreases)
- May contain other bugs, use with caution

## License
MIT
