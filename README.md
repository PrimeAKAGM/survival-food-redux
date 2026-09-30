# Survival Food Redux

Survival Food Redux is a Fabric mod that expands Minecraft's food system with a nutrition mechanic based on four food groups.

Instead of treating every food only as hunger and saturation, different foods contribute to different nutritional values. Maintaining a varied diet can provide useful effects, while deficiencies or an unbalanced diet can have consequences.

The goal is to make food choice more meaningful while keeping the system simple and integrated with normal Minecraft gameplay.

## Features

### Nutrition System

Survival Food Redux tracks four nutrition groups for each player:

- Protein
- Fiber
- Sugar
- Fat

Different foods contribute different amounts to one or more of these groups.

Nutrition values range from 0 to 100 and are tracked individually for each player.

### Nutrition HUD

Nutrition levels are displayed directly on the Minecraft HUD using familiar item icons:

- Cooked Mutton — Protein
- Carrot — Fiber
- Honey Bottle — Sugar
- Golden Apple — Fat

The number of displayed icons provides a quick indication of the current nutrition level.

Very low nutrition levels are also visually highlighted.

### Diet Screen

Press the configurable nutrition keybind (Z by default) to open the Diet screen.

The screen displays:

- Current Protein level
- Current Fiber level
- Current Sugar level
- Current Fat level
- Nutrition percentages
- Visual nutrition bars

### Diet Effects

Your diet can affect your character.

Maintaining certain nutrition levels can provide positive effects, while deficiencies or heavily unbalanced diets can cause negative effects.

Possible positive effects include:

- Strength
- Speed
- Resistance
- Regeneration

Possible negative effects include:

- Weakness
- Slowness
- Hunger

This encourages maintaining a varied diet instead of relying on a single food source.

### Multiplayer Support

Nutrition is tracked independently for each player.

Nutrition data is synchronized between the server and client so that the HUD and Diet screen always reflect the player's current nutrition values.

## Installation

1. Install Fabric Loader.
2. Install Fabric API.
3. Download Survival Food Redux.
4. Place the `.jar` file inside your Minecraft `mods` folder.
5. Launch Minecraft using Fabric.

## Requirements

- Minecraft 26.3
- Fabric Loader
- Fabric API

## Credits and Original Project

Survival Food Redux is based on and inspired by the original **Smart Survival** mod created by **Nitin-2468-dev**.

Original project:
https://github.com/Nitin-2468-dev/smart_survival

Full credit goes to **Nitin-2468-dev** for the original Smart Survival project, its concepts, and the code that provided the foundation for Survival Food Redux.

Survival Food Redux is a separate project maintained by **primegm**. It reworks and expands the original nutrition system with a stronger focus on food and diet mechanics, while removing systems that fall outside the scope of this project, such as temperature and sleep mechanics.

Survival Food Redux is not an official continuation of Smart Survival and is not affiliated with or maintained by the original developer.

## Author

**primegm**