# LifeSteel

A Minecraft Spigot/Paper plugin that will allow you to turn your regular Minecraft server into the Lifesteal SMP!

## Features

- **Player Banning on Death**: Players are banned upon death but can be revived
- **Heart System**: Players lose hearts when they die, but can gain them by defeating other players
- **Revival Mechanics**: Banned players can be revived using special Revive Beacons
- **Custom Recipes**: Craft revival items and hearts with rare materials

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/setmaxhealth <player> <health>` | `lifesteel.sethealth` | Set a player's maximum health |
| `/giverevivebeacon <player> [amount]` | `lifesteel.giverevivebeacon` | Give a player Revive Beacons |
| `/giveheart <player> [amount]` | `lifesteel.giveheart` | Give a player Hearts |
| `/reviveplayer <player>` | `lifesteel.reviveplayer` | Administratively revive a banned player |
| `/revivebeacon` | None | View the Revive Beacon recipe |
| `/heart` | None | View the Heart recipe |

## Recipes

### Revive Beacon
Used to revive banned players:
- 4x Emerald Blocks (corner slots)
- 4x Netherite Ingots (side slots)
- 1x Beacon (center slot)

### Heart
Used to increase max health:
- 4x Diamond Blocks (corner slots)
- 4x Netherite Scraps (side slots)
- 1x Wither Rose (center slot)

## Mechanics

- When a player kills another player, the victim's max health decreases (configurable amount)
- PvP kills have a chance to drop hearts (configurable chance)
- Players can consume hearts to gain back max health
- Revive Beacons can be used to bring banned players back
- Revived players return with reduced maximum health

## Configuration

```yaml
# The max health a player gets when revived (3 hearts)
revived-max-health: 6.0
# Maximum health a player can reach (20 hearts)
max-health-limit: 40.0
# Amount of max health lost when a player dies (1 heart)
health-decrease-on-death: 2.0
# 100% chance to drop a heart on PVP kill
heart-drop-chance: 1.0
# Amount of health points added per heart (1 heart)
heart-increase-amount: 2.0
# Whether to heal the player when using a heart
heal-on-heart-use: true
# Whether to notify the killer that a heart was dropped
notify-heart-drop: true
```

## Installation

1. Download the latest release from the releases page
2. Place the .jar file in your server's plugins folder
3. Restart your server
4. Configure the plugin to your liking in the config.yml file (plugins/LifeSteel/config.yml)
