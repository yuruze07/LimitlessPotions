# LimitlessPotions

A Minecraft Paper plugin that allows unlimited potion upgrading with diminishing returns.

## Features
- Upgrade potion levels with Glowstone Dust (duration reduced to 15%)
- Extend potion duration with Redstone (diminishing returns based on level and current duration)
- Instant health/damage potions are excluded
- Unlimited upgrades (configurable)
- Custom potion lore showing level and duration

## Requirements
- Paper 1.21.1 or higher
- Java 21

## Installation
1. Download the latest JAR from the Releases section
2. Place in your server's `plugins` folder
3. Restart your server

## Configuration
Edit `plugins/LimitlessPotions/config.yml` to adjust:
- Duration reduction percentage for level upgrades
- Base addition time for duration upgrades
- Diminishing return factors
- Max level/duration limits

## Building from source
```bash
mvn clean package
```
The JAR will be in `target/LimitlessPotions-1.0.0.jar`

## License
MIT
