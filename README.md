# Elemental : Reactions

**Now supports 1.21.1 and NeoForge 26.2!**

*中文说明在下方 · Chinese description below*

Welcome to **Elemental : Reactions**! This Minecraft mod introduces a new elemental attribute system, reaction mechanics, dedicated enchantments, and visual effects on top of vanilla survival and combat.

## ✨ Feature Overview

- **4 base elements** — Fire, Nature, Thunder, Frost, with a full counter chain
- **12 dedicated enchantments** — weapon Aspect + armor Enhancement / Resistance for every element
- **6 new status effects** — Wetness, Flammable Spores, Static Shock, Paralysis, Frostbite, Freeze
- **30+ elemental reactions** — steam clouds, spore detonation, paralysis, freeze, and blood-triggered counters
- **Elemental mob AI** — element-aligned mobs gain enchanted equipment, tactical potions, and even spell-caster behavior
- **Biome element bias** — spawned mobs lean toward the element of their biome
- **Graded visual effects** — melee arcs, projectile trails, and impact bursts that scale with enhancement points
- **Admin command system** — `/elementalcraft`, with Tab completion and hot-reloadable config files
- **Mod integrations** — Iron's Spellbooks spell reactions, L_Ender's Cataclysm wetness compatibility

**If you have good ideas or encounter problems, please submit an issue on [GitHub](https://github.com/Accidey/elemental) — I will review and resolve it as soon as possible.**

## 🌟 Core Elements

The mod includes 4 base elements + None:

| Element | ID | Color |
|---------|-----|-------|
| **Fire** | fire | Red |
| **Nature** | nature | Green |
| **Thunder** | thunder | Purple |
| **Frost** | frost | Blue |
| **None** | none | White |

Each element has a Strike enchantment (weapon), an Enhancement enchantment (armor), and a Resistance enchantment (armor).

## ⚔️ Enchantment System

The mod adds 12 dedicated enchantments (3 per element) for weapons and armor:

### Weapon Enchantments

- **Fire Aspect / Nature Aspect / Frost Aspect / Thunder Aspect**: imbues the weapon with the corresponding element, dealing additional elemental damage to enemies
  - Can be applied to all weapons that deal damage (previously limited to swords, axes, tridents, bows, and crossbows)
  - Mutually exclusive with Fire Aspect (vanilla), Flame, and Channeling
  - Different elemental Aspect enchantments are also mutually exclusive

### Armor Enchantments (Enhancement + Resistance)

- **Fire / Nature / Frost / Thunder Enhancement**: increases the wearer's enhancement stat for the corresponding element (fixed percentage per level)
- **Fire / Nature / Frost / Thunder Resistance**: reduces incoming elemental damage of the corresponding type
  - Can only be applied to armor (helmet, chestplate, leggings, boots)
  - Enhancement and Resistance of the same element can coexist on the same piece
  - Different Enhancement enchantments are mutually exclusive; same for Resistance
  - Max level is dynamically calculated from config (max stat cap / points per level)

All enchantments are obtainable through enchanting tables, villager trading, loot chests, and commands.

## 🔁 Element Restraint

Elements have a restraint relationship (configurable):

- **Default chain**: Fire → Nature → Thunder → Frost → Fire
  - Example: Fire attacking a Nature target → 1.5x elemental damage; Fire attacking a Frost target → 0.5x elemental damage
- The Jade info panel shows element relationships (Restrains / Weak / None) for entities

## 🧪 Status Effects

The mod adds 6 new status effects. Duration, damage, and other parameters are configurable.

### Wetness

A stacking effect that acts as a **catalyst for elemental reactions**.

- **Gained from**: water, rain, snow, splash water bottles, and low-heat steam clouds (condensation)
- **Drying**: decays over time away from water sources — speed is affected by biome temperature and nearby heat sources (fire, lava, campfires, furnaces); hot biomes dry faster, cold biomes slower
- **Interactions**:
  - Required for Paralysis (with Static Shock) and Freeze (with Frostbite); can also convert to Spores
  - Instantly removed by Fire and Scorched
  - Increases hunger exhaustion while wet

### Flammable Spores

A stacking parasitic infection that deals periodic poison damage and corrodes equipment durability.

- **Gained from**: Nature attacks (Nature Parasite), Spore Contagion, or converting Wetness
- **Interactions**:
  - Suppressed by Frostbite (damage and corrosion paused, duration decays faster); completely cleared by Freeze
  - Detonated by Fire and Static Shock → Toxic Blast (damage and range scale with stack count; low stacks cause a weak ignite instead)
  - Duration is affected by the target's own element: Fire reduces it; Nature, Thunder, and Frost increase it; cold biomes also reduce it
- **Spreading**: at high stacks, spreads to nearby hostile mobs
- **Immunity**: entities with high Nature Resistance, or on the blacklist

### Static Shock

A stacking electrical charge that deals random damage periodically.

- **Aura**: at high stacks, forms a Static Aura that damages nearby entities, detonates Spores, ignites Creepers, and forces mobs to flee
- **Interactions**:
  - Conducts electricity when wet (triggers Paralysis or Water Electrification)
  - Can also break Freeze
  - Damage type and multiplier depend on the target's own element
- **Immunity**: entities with high Thunder Resistance, or on the blacklist

### Paralysis

A powerful crowd-control effect, triggered by Static Shock + Wetness.

- **Immobilizes** the target completely: unable to move, attack, cast spells, or use items; mob AI is disabled
- **In water**: sinks at 1 block per second and takes drowning damage every second
- **Immunity**: blacklisted entities

### Frostbite

A stacking frost infection that deals periodic ice damage.

- **Slows**: reduces movement/attack speed, scaling with stacks (up to 90% slow)
- **Aura**: at high stacks, forms a Frostbite Aura that applies temporary Frostbite to nearby entities, freezes wet targets, clears Scorched, and forces fleeing
- **Suppression**: suppressed by heat sources; instantly cleared by fire or Scorched; duration decays faster in hot biomes and near heat sources
- **Immunity**: entities with high Frost Resistance, or on the blacklist

### Freeze

A devastating crowd-control effect, triggered by Frostbite + Wetness.

- **Immobilizes** the target completely (no movement, attack, or AI); deals periodic ice damage
- **Ice shell**: blocks all physical melee and projectile damage — elemental attacks (Fire, Frost, Thunder, Nature, ISS magic) penetrate and deal full damage
- **In water**: sinks at 2 blocks per second (twice as fast as Paralysis) and takes drowning damage every second
- **Breaking**:
  - Strong Fire attacks break it and convert it to Wetness
  - Scorched converts it to Steam
  - Static Shock has a chance to break it
- **Also**: clears Spores; can be refreshed by Frost spells
- **Immunity**: blacklisted entities

## 💥 Elemental Reactions

### 🔥 Fire Reactions

#### Self-Drying

**Trigger**: self has Wetness + Fire attack

- Reduces own Wetness stacks; the attack's fire damage is reduced
- If Fire power ≥ the Low-Heat Steam threshold and not on cooldown, spawns a Low-Heat Steam Cloud

#### Scorched

**Trigger**: Fire attack (target not wet, not in a Low-Heat Steam Cloud)

- Applies high-duration fire damage over time (Scorched); immune to vanilla fire
- Contact with water triggers Thermal Shock
- High enough Fire power applies a Scorched Aura that damages entities in range

#### Scorched Aura

**Trigger**: Scorched entity has high enough Fire power

- Periodically damages entities in range and applies temporary Scorched
- Clears Frostbite from nearby entities, detonates Creepers, and forces nearby mobs to flee

#### Poison Boost

**Trigger**: Scorched target has the Poison effect

- Consumes the Poison effect to boost Scorched: extends duration and increases the damage multiplier

#### High-Heat Steam Cloud

**Trigger**: Fire attack + target is Wet

- Spawns a High-Heat Steam Cloud that periodically scalds entities inside (fire damage, scales with level)
- Clears Wetness and Frostbite from entities inside; applies Blindness; clears aggro
- Fire-immune entities take reduced damage

#### Low-Heat Steam Cloud

**Trigger**: generated from Self-Drying, Thermal Shock, or Freeze→Steam

- Slowly applies Wetness (condensation) and Blindness to entities inside
- Can be electrified by Static entities (→ damage + Paralysis) or frosted by Frostbite entities (→ freezes entities inside)
- Electrified / Frosted clouds prevent further condensation

### 🌿 Nature Reactions

#### Nature Parasite

**Trigger**: Nature attack (chance scales with Nature Enhancement; Wetness bonus, stacking bonus)

- Applies Flammable Spores stacks
- Immune if Nature Resistance ≥ threshold; frozen targets are immune

#### Spore Contagion

**Trigger**: Spore stacks reach the threshold (default 3)

- Spreads repeatedly (every 20 ticks) while the source's spore effect lasts
- Transferred stacks = source stacks − 2 (min 1); radius grows with stacks
- The target's own Wetness converts to Spores first
- Each entity can only be infected once per spore effect, and never infects back its original source; re-infection is possible after the effect ends
- By default spreads to any nearby entity without Spores (including players and tamed animals); a config option restricts it to hostile mobs
- Infected entities do not spread further by default (chain contagion is configurable)

#### Toxic Blast

**Trigger**: Scorched target has Flammable Spores

- **Low stacks (< threshold)**: weak blast — applies reduced Scorched without an explosion
- **High stacks (≥ threshold)**: full explosion — area damage with radius scaling by Spore stacks, applies enhanced Scorched to all nearby entities
- Can chain-detonate nearby targets with Spores ≥ threshold

### ⚡ Thunder Reactions

#### Static Attachment

**Trigger**: Thunder attack (chance scales with Thunder Enhancement; Wetness bonus, stacking bonus)

- Applies Static Shock stacks; higher chance on wet targets
- Immune if Thunder Resistance ≥ threshold

#### Static Aura

**Trigger**: Static stacks ≥ aura threshold

- Periodically damages entities in range
- Paralyzes wet targets, detonates Spores, breaks Freeze, detonates Creepers, and forces nearby mobs to flee

#### Water Electrification

**Trigger**: Static entity enters/stands in water

- Electrifies a zone of water, dealing settlement damage and Paralysis to all entities in the water
- Clears the source entity's Static and Wetness

#### Static Spore Blast

**Trigger**: an entity with Static (or in Static Aura range) has Spores

- Chance to detonate Spores based on Static and Spore stacks

#### Paralysis

**Trigger**: Static target gains Wetness (or Static Aura hits a wet target)

- Clears Static and Wetness, converts to Paralysis, and deals the remaining Static damage

#### Static Steam Cloud

**Trigger**: Static entity enters a Low-Heat Steam Cloud

- Electrifies the steam cloud, dealing settlement damage and Paralysis to all entities inside

### ❄️ Frost Reactions

#### Frostbite

**Trigger**: Frost attack (chance scales with Frost Enhancement; Wetness bonus, stacking bonus)

- Applies Frostbite stacks: periodic ice damage and reduced movement/attack speed
- High enough stacks → Frostbite Aura

#### Frostbite Aura

**Trigger**: Frostbite stacks ≥ aura threshold

- Applies temporary Frostbite to entities in range (slows and deals periodic ice damage)
- Freezes wet targets
- If the target is Scorched: clears Scorched and forces fleeing (no Frostbite applied)

#### Freeze

**Trigger**: Frostbite + target has Wetness

- Completely immobilizes the target (AI disabled, periodic ice damage)
- Physical attacks are blocked by the ice shell; elemental attacks penetrate and deal attribute damage
- Fire attacks (strong enough) break the Freeze and convert it to Wetness
- Generates a cold mist cloud on freeze

#### Fire Freeze Melt

**Trigger**: Fire attack + target is Frozen (Fire power ≥ threshold)

- Instantly breaks the Freeze; clears Scorched; converts to Wetness (stacks = freeze stacks)

#### Freeze→Steam

**Trigger**: a Frozen entity gains Scorched (via aura or Fire attack)

- Scorched melts the freeze, generating a Low-Heat Steam Cloud

#### Frostbite→Wetness

**Trigger**: Scorched + target has Frostbite

- Converts Frostbite stacks to Wetness stacks; clears both Scorched and Frostbite

#### Frosted Steam Cloud

**Trigger**: a Frostbite Aura entity enters a Low-Heat Steam Cloud

- The steam cloud becomes frosted, freezing entities inside

#### Frostbite Suppresses Spores

**Trigger**: entity has both Frostbite and Spores

- Spores are suppressed during Frostbite; Spore duration decays faster
- Spores resume when Frostbite ends

#### Scorched Aura Clears Frostbite

**Trigger**: Scorched Aura hits a Frostbite / temporary Frostbite target

- Clears the Frostbite state and forces fleeing

#### Condensation

**Trigger**: standing in a Low-Heat Steam Cloud

- Slowly accumulates Wetness stacks
- Electrified / Frosted steam clouds prevent condensation

### Universal Reactions

#### Thermal Shock

**Trigger**: Scorched target contacts water

- Instantly deals the remaining Scorched damage and clears Scorched
- Generates a Low-Heat Steam Cloud; vaporizes water at the contact point

### 🩸 Elemental Counters (Blood-Triggered Reversal)

When an element-aligned entity takes damage that would drop its HP below a configurable **blood threshold**, it may trigger a powerful counter-attack. All counters share the same framework:

- The entity's element must match the counter's element
- The entity's corresponding Enhancement points must reach the counter's strength threshold
- After triggering, the counter enters a **health-recovery cooldown**: the entity cannot counter again until its HP heals back above a configurable recovery threshold

#### Fire Counter

**Trigger**: Fire-aligned entity at low HP (Fire Enhancement ≥ threshold; Fire Resistance below the Scorched immunity threshold)

- Locks itself in place with **90% damage reduction against ALL incoming damage**
- A contracting fire ring pulls nearby entities toward the center
- Then explodes: area knockback + Fire damage + unconditional Scorched applied to all affected entities, with a fire tornado visual

#### Nature Counter

**Trigger**: Nature-aligned entity at low HP (Nature Enhancement ≥ threshold)

- An eruption knocks back all nearby enemies and applies Spores to them
- Optionally clears the victim's own Scorched state (configurable)

#### Thunder Counter

**Trigger**: Thunder-aligned entity at low HP (Thunder Enhancement ≥ threshold)

- Summons a localized thunderstorm that expands to max radius: periodic lightning strikes deal lightning damage
- Applies max-stack Paralysis and additional Static stacks, and forces Wetness on affected entities over time

#### Frost Counter

**Trigger**: Frost-aligned entity at low HP (Frost Enhancement ≥ threshold)

- Triggers an ice burst (Frost Burst) centered on the entity; the burst expands to max radius, then lingers for 40 ticks
- Entities within the area: Frostbite is applied (scaled by the caster's Frost Enhancement), and wet targets are additionally Frozen
- Also spawns a frosted steam cloud

All counter parameters (blood thresholds, strength thresholds, recovery ratios, radii, damage) are configurable.

## 🌍 Biome Element Bias

Different biomes grant element tendencies to spawned mobs:

- 🔥 **Hot biomes** (Desert, Badlands, Nether, etc.) → Fire bias
- ❄️ **Cold biomes** (Snowy Plains, Ice Spikes, etc.) → Frost bias
- 🌲 **Forest / Jungle biomes** → Nature bias
- ⛈️ **Thunderstorm weather** (global) → Thunder bias

## 🧟 Mob Attribute System

Mobs can gain elemental attributes at spawn through a priority-based system:

### Attribute Assignment Priority

1. **Blacklist**: mobs in the attribute blacklist never gain any attributes
2. **Forced Attributes (Commands)**: if an admin has configured forced attributes for a mob type via commands (`/elementalcraft entity add`), those attributes are applied with highest priority
3. **Dimension Defaults**: if no forced attributes exist:
   - **Nether**: mobs default to Fire attribute (configurable points)
   - **End**: mobs default to Thunder attribute (configurable points)
4. **Random Generation (chance-based)**:
   - **Hostile mobs** (Monster) have a configurable chance to gain attributes
   - **Neutral mobs** (NeutralMob, Piglins) have a separate, lower chance
   - The assigned element is determined by **biome element bias** (e.g. Desert → Fire, Snowy Plains → Frost)
   - Each mob can gain: **Attack element** (chance-based), **Enhancement points** (distributed across 4 armor slots), and **Resistance points** (distributed across 4 armor slots)
   - The resistance type has a configurable chance to be the attack element's **counter element** (e.g. a Fire mob with Frost resistance)

### Equipment & Durability

Mobs with an attack element receive:

- **Weapon**: a random weapon (sword, axe, pickaxe, shovel, etc.) with the corresponding **elemental Aspect enchantment** and **Unbreaking III**
- **Already holding a weapon**: the enchantments are applied directly to the existing weapon, without replacing it
- **Armor**: automatically generated if missing, with **Enhancement** and/or **Resistance enchantments** + **Unbreaking III**; points are distributed proportionally across the 4 armor slots

All equipment drop chances default to 0% (equipment does not drop). Attribute data is stored in NBT for enchanted book drops.

### Enchanted Book Drops

- Element-aligned mobs have a chance to drop **enchanted books** on death (affected by Looting)
- Books may contain the mob's:
  - **Aspect enchantment**
  - **Enhancement enchantment** (random level based on enhancement points)
  - **Resistance enchantment** (random level based on resistance points)
- Book level weighting favors the mob's actual attribute quality

### Dimension Defaults

- **Nether**: all mobs default to Fire attribute (configurable)
- **End**: all mobs default to Thunder attribute (configurable)

### Mob Flee Behavior

- Mobs near entities with a **Scorched Aura**, **Frostbite Aura**, or **Static Aura** will attempt to **flee** (path away from the aura source; can be disabled in config)
- Flee mechanics:
  - Mob AI is temporarily disabled (`setNoAi(true)`) and the current attack target is cleared
  - The mob calculates an escape path away from the aura source, avoiding obstacles
  - If stuck (5 ticks with less than 0.05 blocks moved), it attempts to jump over 1-block-high obstacles or teleport to a valid standing position
  - Fleeing lasts up to **200 ticks (10 seconds)**, stopping when the target point is reached or the aura source is far enough away
- Suppression: fleeing is disabled if the mob is immobilized by Iron's Spellbooks **Root** spell

### Tactical Potion Throwing (all element-aligned mobs)

- **All mobs that have an attack element** (Fire, Nature, Thunder, Frost) and are **not ISS casters** have a configurable chance to equip a splash potion in their offhand
- Potion type depends on the mob's element:
  - **Fire-aligned mobs** equip a **Splash Potion of Poison** (to trigger the Poison Boost effect for Scorched)
  - **Nature, Thunder, and Frost-aligned mobs** equip a **Splash Water Bottle** (to apply Wetness — required for the Nature Parasite bonus, Paralysis, or Freeze reactions)
- Throwing behavior:
  - The mob throws the potion when the target is within 12 blocks, has line of sight, is not already affected by the desired effect, and is not Scorched (for water bottles)
  - After throwing, it waits 20 ticks to check the result:
    - **Success**: the mob continues normal combat
    - **Failure (effect not applied)**: counts as a miss
  - After **3 consecutive misses**, the mob gives up and stops throwing potions (enters a long cooldown)
- If `wetnessNetherDimensionImmune` is enabled, water bottles are not equipped in the Nether (poison potions may still be used depending on config)

## 🛠️ Command System (Admin/OP)

All commands start with `/elementalcraft` and support Tab completion.

### Debug Mode

```
/elementalcraft debug      # Toggle debug mode: shows elemental damage calculation and reaction info
```

### Reload Config

```
/elementalcraft reload     # Reload all config caches from disk
```

### Biome Bias Management

```
/elementalcraft biome add <element> <probability>
/elementalcraft biome remove <element>
/elementalcraft biome list
```

### Forced Entity Attributes

```
/elementalcraft entity add <attack element> [enhancement element] [enhancement points] [resistance element] [resistance points]
/elementalcraft entity remove        # Clears forced attributes for the held spawn egg
```

- Example: `/elementalcraft entity add fire fire 50 frost 20`
- Points support fixed values (e.g. `50`) or ranges (e.g. `20-80`)
- All parameters after the attack element are optional (default: none, 0, none, 0)

### Entity Attribute Blacklist

```
/elementalcraft entity blacklist add <element>      # Held spawn egg: prevent this entity from gaining the specified element (or "all" for all elements)
/elementalcraft entity blacklist remove <element>
/elementalcraft entity blacklist list
```

### Forced Item Attributes

Weapons:

```
/elementalcraft item weapon add <element>      # Bind attack attribute (held weapon)
/elementalcraft item weapon remove             # Remove forced weapon attribute (held weapon)
```

Armor:

```
/elementalcraft item armor add <enhancement element> [enhancement points] [resistance element] [resistance points]
/elementalcraft item armor remove              # Remove forced armor attributes (held armor)
```

- Enhancement element is required; points, resistance element, and resistance points are optional (default: 0, none, 0)

### Effect Immunity Blacklist

```
/elementalcraft blacklist scorched add|remove|list     # Using Scorched as an example
```

- Available effects: scorched, spore, static, paralysis, frostbite, freeze, steam, wetness

### Config File Operations

- Changes made via commands are automatically saved and hot-loaded — no server restart needed
- Directly edit TOML files in `config/ElementalCraft/`; the mod auto-detects and refreshes configuration every 100 ticks

## ✨ Visual Effects (graded by enhancement points)

When enhancement points reach the threshold (maxStatCap from config), visual effects trigger:

### Melee Swing Effects

- **Fire**: flame arc (soul fire, lava, smoke particles at higher levels)
- **Nature**: compost particles, spore blossoms (cherry blossoms, wax particles at higher levels)
- **Frost**: ice rune particles, snowflake particles (frost overlay, ice layer, snow layer at higher levels)
- **Thunder**: glow particle arc (reverse portal, lightning arc at higher levels)

### Ranged Projectile Effects

- **Fire**: double helix flame trail (outer flame, inner soul fire, lava tail particles)
- **Nature**: cherry blossom spiral trail (villager happy particles at the tail)
- **Frost**: snowflake spiral trail (frost overlay, ice rune particles at higher levels)
- **Thunder**: lightning spark spiral trail (end candle, reverse portal, dragon breath at higher levels)

### Impact Explosion Effects

- Corresponding elemental particle burst (fire/lava, cherry/spore blossom, ice rune/snowflake, glow particles/end candles, etc.)
- Effect intensity scales linearly with level; max-level effects are visually impactful. Configurable in `elementalcraft-visuals.toml`

### Frost-Exclusive Visuals

- **Frostbite Overlay**: screen overlay when an entity has Frostbite
- **Frost Snow Layer**: snow rendering on frozen entities
- **Freeze Ice Layer**: ice shell visual effect on frozen entities

## ⚙️ Configuration Files (server/client)

Config files are located in `config/ElementalCraft/`:

| File | Purpose |
|------|---------|
| **elementalcraft-common.toml** | Element restraints, damage multipliers, biome bias, enchantment stats, forced entities/blacklists, dimension attributes, etc. |
| **elementalcraft-forced-items.toml** | Forced item attributes (can be added via commands) |
| **elementalcraft-fire-nature-reactions.toml** | Fire and Nature reaction parameters (Scorched, Spores, Steam, Toxic Blast, Fire/Nature Counters, etc.) |
| **elementalcraft-thunder-frost-reactions.toml** | Thunder and Frost reaction parameters (Static, Paralysis, Frostbite, Freeze, Water Electrification, Thunder/Frost Counters, etc.) |
| **elementalcraft-visuals.toml** | Particle effect toggles, density, angles, speed, etc. |

Hot-reload supported: configurations refresh automatically after saving.

## 🔗 Iron's Spellbooks Integration

When **Iron's Spellbooks** is installed alongside Elemental : Reactions, additional integration features are enabled. All spell reaction chances and intensities are calculated based on the caster's corresponding element enhancement points, with the best spell level matched to the caster's power.

### Caster Mob Spawning

- Mobs with an attack element have a chance to become **ISS spell caster mobs** at spawn
- Caster spawn chance is controlled by the unified config value `casterMobChance` (shared across all elements)
- Caster type is tied to the attack element (Thunder → Thunder caster, Nature → Nature caster, etc.)
- A caster blacklist can prevent specific entities from becoming casters
- **Nature casters** with the Acid Orb spell equip the scroll in the offhand and a random weapon in the main hand
- **Frost casters** with the Summon Polar Bear spell equip the scroll in the offhand and a random weapon in the main hand

### Fire Spell Reactions

- Fire spells (Firebolt, Fireball, Burning Dash, Magma Bomb, Flaming Barrage, Flaming Strike, Scorch, Heat Surge, Blaze Storm, Fire Breath, Fire Arrow) have a chance to apply **Scorched** on hit, replacing ISS's built-in fire effect
- **Blaze Storm** fires multiple small fireballs; each can trigger Scorched independently, but with a cooldown between triggers
- **Flaming Strike** and **Raise Hell** have dedicated handlers that trigger Scorched
- **Fire Field / Wall of Fire** continuously applies Scorched over time to entities inside, in this order:
  - First checks **Fire Freeze Melt** on frozen targets
  - Then checks **Wetness** for a High-Heat Steam Cloud
  - Finally applies Scorched
- If the target has **Poison** or **Flammable Spores**, the Scorched trigger chance is boosted to **100%** and damage is amplified

### Nature Spell Reactions

- Nature spells (Acid Orb, Poison Arrow, Earthquake, Firefly Swarm, Poison Spray, Poison Splash, Root, Stomp) trigger **Nature Parasite** (apply Flammable Spores) on hit
- **Poison-type spells** (Poison Arrow, Poison Spray, Poison Splash) do **not** apply Spores — they only apply vanilla Poison
- **Acid Orb** applies Spores in an AoE (3.5 block radius) on impact, affecting all nearby entities
- **Root**:
  - Applies initial Spores on hit
  - While the target remains Rooted, additional Spores are applied continuously over time
  - If a Rooted target is hit by **Scorched**, Root is removed and the Scorched duration and damage are enhanced (same as Poison Boost)

### Thunder Spell Reactions

- Thunder spells (Lightning Lance, Chain Lightning, Ball Lightning, Electrocute, Lightning Bolt, Shockwave, Thunderstorm, Ascension, Volt Strike) apply **Static Shock** stacks based on the caster's Thunder Enhancement
- If the target has **Wetness** → triggers **Paralysis** (clears Wetness, converts to Paralysis)
- If the target is **in water** → triggers **Water Electrification**: creates a persistent electrified water zone that periodically damages and paralyzes entities in the water
- If the target has **Flammable Spores** → triggers **Spore Blast**
- If the target is in a **Low-Heat Steam Cloud** → electrifies the cloud, dealing damage and Paralysis to entities inside
- **Electrocute** can refresh Paralysis duration on paralyzed targets; the caster enters a cooldown after the spell ends
- Vanilla **Lightning Bolt** hitting a wet target: triggers Water Electrification if available; otherwise applies max Static Shock stacks and resolves the Wetness conflict

### Frost Spell Reactions

- Frost spells (Cone of Cold, Icicle, Ray of Frost, Frostwave, Ice Spikes, Snowball, Frostbite, Blizzard, Ice Tomb, Summon Polar Bear) have a chance to apply **Frostbite** based on the caster's Frost Enhancement
- **Blizzard** AoE continuously applies Frostbite every second to all entities inside
- **Ice Tomb** is specially handled for caster mobs — directly traps the target for a short duration
- If the target has **Wetness** → triggers **Freeze** (Frostbite + Wetness → Freeze)
- Frost spells hitting an already-frozen target **refresh** the Freeze duration, keeping the target immobilized longer
- ISS's **Chilled** effect combined with Wetness converts to **Freeze** automatically
- **Polar Bear** casters track their summoned bear and re-summon if killed (up to 3 times); bears are removed when the caster dies

### Poison Cloud + Fire Reaction

- When a **Scorched target** enters an ISS **Poison Cloud**, the cloud enhances the Scorched duration and damage multiplier (Poison Boost), then dissipates with a puff effect
- Scorched Aura entities near a Poison Cloud can also trigger this reaction

### ROOT Spell Integration

- The **ROOT** spell applies **Flammable Spores** stacks on hit based on the caster's Nature Enhancement
- While ROOTed, additional Spores are applied over time
- If a ROOTed target is hit by **Scorched**, ROOT is removed and the Scorched duration and damage are enhanced (same as Poison Boost)
- ROOT immobilization suppresses mob flee behavior — affected mobs stop fleeing

### Caster Mob AI

All 4 element caster mobs share the following behaviors:

- Automatically equipped with the corresponding element's **spell scroll** and **elemental Aspect enchantment** at spawn
- Spell level is selected based on the mob's enhancement points via a rarity-matching algorithm to pick the most appropriate level
- Enters **aggressive mode** (more frequent casting) below **50%** health
- Scroll drop chance is configurable (default: 100%)
- **Drop enchantment cleansing**: items with elemental Aspect enchantments dropped by ISS caster mobs are automatically de-enchanted (prevents infinite farming of elemental enchantments from mob kills)

#### Fire Casters

- Equipped with a **Splash Potion of Poison** in the offhand
- Throws poison at the target before casting spells:
  - If the target is wet, a High-Heat Steam Cloud triggers first (consumes Wetness)
  - Once Wetness is cleared, Poison ensures a **100% Scorched trigger chance** and enhances duration
- Enters a cooldown if the poison fails to apply multiple times

#### Nature Casters

- **Do not throw any potions**
- **With Acid Orb** — special AI loop:
  - Casts, waits for the hit, then checks if the target has the **REND** effect
  - If REND is applied: switches to melee and waits for REND to end
  - If not: retries casting; 2 consecutive misses trigger a cooldown
- **With other spells**: standard casting, checks target paralysis immunity

#### Thunder Casters

- Equipped with a **Splash Water Bottle** in the offhand
- Throws water to apply Wetness before casting, to trigger Paralysis
- Tracks the target's Wetness status; only throws if the target is not already wet
- If the target remains dry after throwing, it counts as a miss; 2 misses trigger a cooldown
- Does not equip water bottles in the Nether if `wetnessNetherDimensionImmune` is enabled

#### Frost Casters

- Equipped with a **Splash Water Bottle** in the offhand (**Summon Polar Bear** casters excepted)
- Throws water to apply Wetness before casting, to trigger Freeze
- Same miss-tracking as Thunder casters
- **Summon Polar Bear** casters do not equip water bottles: after casting they check if the bear is alive (48-block range) and re-summon if dead (up to 3 times); bears are removed when the caster dies
- Does not equip water bottles in the Nether if `wetnessNetherDimensionImmune` is enabled

### Other Integration Details

- **Non-aggressive spell exclusion**: Heat Surge, Acid Orb, Oakskin, Fire Breath, Cone of Cold, and Electrocute do not trigger aggressive casting
- **Scroll rarity matching**: mobs calculate the best spell level from their enhancement points by matching the closest rarity value
- **Player spell tracking**: the last spell ID, level, and cast source are tracked on the player's NBT data, used for reaction calculations on subsequent hits
- **Thunder spell enchantment handling**: Thunder caster mob spells temporarily save and clear enchantments on the target's items during damage calculation, then restore them after, ensuring accurate damage tracking

## 🔗 L_Ender's Cataclysm Integration

When **L_Ender's Cataclysm** is installed, Cataclysm's Wetness effect is replaced by the Wetness system of Elemental : Reactions:

- Wetness applied by Cataclysm (e.g. wetness-inflicting attacks) becomes the Wetness of Elemental : Reactions instead — with the same stacking, natural decay, and access to all elemental reactions (steam, paralysis, freeze, spores, etc.)
- Consecutive hits stack the Wetness higher, up to the usual maximum
- Cataclysm's "Wetness + Lightning" bonus is preserved: lightning deals extra damage against wet targets (up to +100%)
- Cataclysm bosses can be affected by Elemental : Reactions effects
- The mod has no effect when Cataclysm is not installed

---

# 属性锻造：元素反应

**现已支持 1.21.1 和 NeoForge 26.2**

欢迎来到《属性锻造：元素反应》！这是一款围绕元素战斗打造的 Minecraft 模组，为原版生存与战斗玩法新增了全新的属性系统、元素反应机制、专属附魔以及炫酷的视觉特效。

## ✨ 特性一览

- **4 种基础元素**——赤焰、自然、雷霆、冰霜，附带完整克制链
- **12 种专属附魔**——每系武器的"属性"附魔 + 防具的"强化 / 抗性"附魔
- **6 种全新状态效果**——潮湿、易燃孢子、静电、麻痹、霜冻、冻结
- **30 余种元素反应**——蒸汽云、毒火爆燃、麻痹、冻结、濒血反制等
- **元素生物 AI**——带属性生物会自动获得附魔装备、战术药水，甚至成为施法者
- **群系元素偏向**——生成生物的元素倾向由所在群系决定
- **分级视觉特效**——近战弧光、弹道轨迹、撞击爆发，随强化点数逐级增强
- **管理员指令系统**——`/elementalcraft`，支持 Tab 补全与配置热加载
- **模组联动**——Iron's Spellbooks 法术反应、L_Ender's Cataclysm 潮湿兼容

**如果你有好的想法或遇到问题，请在 [GitHub](https://github.com/Accidey/elemental) 上提交 issue，我会尽快查看和处理。**

## 🌟 核心元素

游戏内包含 4 种基础元素属性 + 无属性：

| 元素 | 标识符 | 颜色 |
|------|--------|------|
| **赤焰** | fire | 红色 |
| **自然** | nature | 绿色 |
| **雷霆** | thunder | 紫色 |
| **冰霜** | frost | 蓝色 |
| **无属性** | none | 白色 |

每种元素均配有攻击附魔、强化附魔和抗性附魔。

## ⚔️ 附魔系统

模组为武器和防具新增了 12 种专属附魔（每种元素对应 3 种）：

### 武器附魔（攻击属性）

- **赤焰属性 / 自然属性 / 冰霜属性 / 雷霆属性**：为武器赋予对应元素的攻击效果，对敌人造成额外的元素伤害
  - 可附魔在所有有伤害的武器上（此前仅限剑、斧、三叉戟、弓和弩）
  - 与火焰附加、火矢、引雷附魔互斥
  - 不同元素的攻击附魔之间也相互排斥

### 防具附魔（强化 + 抗性）

- **赤焰强化 / 自然强化 / 冰霜强化 / 雷霆强化**：提升对应元素攻击的伤害加成（每级为固定百分比）
- **赤焰抗性 / 自然抗性 / 冰霜抗性 / 雷霆抗性**：降低受到的对应元素攻击伤害
  - 仅可附魔在防具（头盔、胸甲、护腿、靴子）上
  - 同一种元素的强化附魔和抗性附魔可以共存
  - 不同元素的强化附魔之间相互排斥，抗性附魔同理
  - 附魔最高等级由配置文件动态计算（最大属性上限 / 每级所需点数）

所有附魔均可通过附魔台、村民交易、战利品宝箱等常规方式获取，也可通过指令强制绑定。

## 🔁 元素克制关系

元素之间存在克制关系（可在配置文件中自定义）：

- **默认克制链**：🔥 赤焰 → 🌿 自然 → ⚡ 雷霆 → ❄️ 冰霜 → 🔥 赤焰
  - 示例：赤焰攻击自然属性目标 → 1.5 倍属性伤害；赤焰攻击冰霜属性目标 → 0.5 倍属性伤害
- 可通过 Jade 信息面板查看实体的元素关系（克制 / 被克制 / 无关系）

## 🧪 状态效果

模组新增了 6 种全新的状态效果。持续时间、伤害等参数均可在配置文件中调整。

### 潮湿（Wetness）

层数型效果，是元素反应的**"催化剂"**。

- **获得方式**：浸水、雨雪、喷溅水瓶、低温蒸汽云（冷凝）
- **干燥**：离开水源后会逐渐衰减，干燥速度受群系温度和附近热源（火、熔岩、营火、熔炉）影响——炎热群系加速干燥，寒冷群系减缓
- **交互**：
  - 触发麻痹（配合静电）和冻结（配合霜冻）的必要条件，也可转化为孢子
  - 火焰和灼烧会立即清除潮湿
  - 潮湿状态下饱食度消耗增加

### 易燃孢子（Flammable Spores）

层数型寄生感染，周期性造成毒伤并腐蚀装备耐久。

- **获得方式**：自然攻击（自然寄生）、孢子传播、潮湿转化
- **交互**：
  - 霜冻会抑制孢子（暂停伤害和腐蚀，加快衰减）；冻结会完全清除孢子
  - 赤焰和静电会引爆孢子 → 毒火爆燃（伤害和范围随层数提升，低层数为弱效点燃）
  - 持续时间受目标自身元素影响：火属性缩短，自然、雷霆、冰霜延长；寒冷群系也会缩短
- **传播**：高层数时会向附近敌对生物传播
- **免疫**：自然抗性极高或处于黑名单中的实体

### 静电（Static Shock）

层数型电荷效果，周期性造成随机伤害。

- **光环**：层数足够时形成静电光环——伤害周围实体、引爆孢子、引燃苦力怕、迫使生物逃跑
- **交互**：
  - 潮湿时会导电（触发麻痹或感电水域）
  - 可破除冰冻
  - 伤害类型和倍率受目标自身元素影响
- **免疫**：雷霆抗性极高或处于黑名单中的实体

### 麻痹（Paralysis）

强力控制效果，由静电 + 潮湿反应触发。

- **禁锢**：完全禁锢目标——无法移动、攻击、施法、使用物品，生物 AI 被禁用
- **水中**：以每秒 1 格的速度下沉，并每秒受到溺水伤害
- **免疫**：黑名单中的实体

### 霜冻（Frostbite）

层数型冻伤效果，周期性造成冰冻伤害。

- **减速**：降低移动速度和攻击速度，层数越高减速越严重（最高 90%）
- **光环**：层数足够时形成霜冻光环——对范围内实体施加临时霜冻、冻结潮湿目标、清除灼烧并迫使逃跑
- **抑制**：受热源抑制，被火焰或灼烧立即清除；炎热群系和热源附近加速衰减
- **免疫**：冰霜抗性极高或处于黑名单中的实体

### 冻结（Freeze）

毁灭性控制效果，由霜冻 + 潮湿反应触发。

- **禁锢**：完全禁锢目标（无法移动、攻击，AI 禁用），并周期性承受冰冻伤害
- **冰壳**：格挡所有物理近战和投射物伤害；元素攻击（赤焰、冰霜、雷霆、自然、ISS 魔法）穿透并造成全额伤害
- **水中**：以每秒 2 格的速度加速下沉（麻痹的两倍），并每秒受到溺水伤害
- **破除**：
  - 强力的赤焰攻击 → 破除并转化为潮湿
  - 灼烧 → 转化为蒸汽
  - 静电有概率破除
- **其他**：冻结会清除孢子；冰霜法术可刷新持续时间
- **免疫**：黑名单中的实体

## 💥 元素反应

### 🔥 赤焰相关反应

#### 自我干燥

**触发**：自身处于潮湿状态 + 赤焰攻击

- 降低自身潮湿层数，本次攻击的赤焰伤害降低
- 若赤焰强度 ≥ 低温蒸汽阈值且不在冷却中，生成低温蒸汽云

#### 灼烧

**触发**：赤焰攻击（目标无潮湿、不在低温蒸汽云中）

- 造成高强度持续火焰伤害（灼烧），免疫普通火焰伤害
- 接触水时触发"热冲击"
- 赤焰强度足够时施加灼烧光环，伤害光环范围内实体

#### 灼烧光环

**触发**：灼烧实体的赤焰强度足够高

- 周期性伤害光环范围内实体，并施加临时灼烧
- 清除附近实体的霜冻，引爆苦力怕，迫使附近生物逃跑

#### 中毒增幅

**触发**：灼烧目标带有中毒效果

- 中毒效果被消耗以增幅灼烧：延长持续时间并提高伤害倍率

#### 高温蒸汽云

**触发**：赤焰攻击 + 目标处于潮湿状态

- 生成高温蒸汽云，周期性烫伤云内实体（火焰伤害，随等级提升）
- 清除云内实体的潮湿和霜冻
- 施加致盲，清除生物仇恨
- 火焰免疫实体受到减伤

#### 低温蒸汽云

**触发**：由自我干燥、热冲击或冻结→蒸汽生成

- 缓慢对云内实体施加潮湿（冷凝）和致盲效果
- 可被静电实体感电化（→造成伤害和麻痹），或被霜冻实体霜寒化（→冻结云内实体）
- 感电 / 霜寒化的云阻止进一步冷凝

### 🌿 自然相关反应

#### 自然寄生

**触发**：自然属性攻击（概率随自然强化点数提升，潮湿加成，叠加加成）

- 附加易燃孢子层数
- 自然抗性 ≥ 阈值则免疫；冻结目标免疫

#### 孢子传播

**触发**：孢子层数达到阈值（默认 3 层）

- 孢子效果持续期间，每 20 刻周期性向附近实体传播
- 转移层数 = 源层数 − 2（最低 1 层），半径随层数增长
- 目标自身的潮湿会先转化为孢子
- 每个实体在同一孢子持续期间只会被传染一次，且不会传染回其原传染源；效果结束后可再次被传染
- 默认传播给范围内所有未携带孢子的实体（包括玩家与驯服动物），可在配置中限制为仅敌对生物
- 被传染者默认不会继续扩散（可在配置中开启链式传染）

#### 毒火爆燃

**触发**：灼烧目标带有易燃孢子

- **低层数（< 阈值）**：弱效爆燃——仅施加伤害降低的灼烧，无爆炸
- **高层数（≥ 阈值）**：完整爆炸——造成范围爆炸伤害，范围随孢子层数增长，对附近所有实体施加增强灼烧
- 可连锁引爆附近孢子 ≥ 阈值的目标

### ⚡ 雷霆相关反应

#### 静电附着

**触发**：雷霆属性攻击（概率随雷霆强化点数提升，潮湿加成，叠加加成）

- 附加静电层数，对潮湿目标概率更高
- 雷霆抗性 ≥ 阈值则免疫

#### 静电光环

**触发**：静电层数 ≥ 光环阈值

- 周期性伤害光环范围内实体
- 可麻痹潮湿目标、引爆孢子、破除冰冻、引爆苦力怕，迫使附近生物逃跑

#### 感电水域

**触发**：静电实体进入 / 站在水中

- 使范围内的水域感电，对水中所有实体造成结算伤害和麻痹效果
- 清除源实体的静电和潮湿

#### 静电孢子引爆

**触发**：带有静电效果或处于静电光环范围内的实体拥有孢子

- 基于静电和孢子层数的概率引爆孢子

#### 麻痹

**触发**：静电目标获得潮湿状态（或静电光环命中潮湿目标）

- 清除静电和潮湿状态，转化为麻痹效果，并结算剩余静电伤害

#### 感电蒸汽云

**触发**：静电实体进入低温蒸汽云

- 使蒸汽云感电，对云中所有实体造成结算伤害和麻痹效果

### ❄️ 冰霜相关反应

#### 霜冻

**触发**：冰霜攻击（概率随冰霜强化点数提升，潮湿加成，叠加加成）

- 施加霜冻层数：周期性冰冻伤害并减速移动 / 攻击速度
- 层数足够时 → 霜冻光环

#### 霜冻光环

**触发**：霜冻层数 ≥ 光环阈值

- 对光环范围内的实体施加临时霜冻（减速并造成周期冰伤）
- 可冻结潮湿目标
- 若目标处于灼烧状态：清除灼烧并迫使其逃跑（不施加霜冻）

#### 冻结

**触发**：霜冻 + 目标有潮湿效果

- 完全禁锢目标（生物 AI 禁用，周期性冰伤）
- 物理攻击被冰壳格挡，元素攻击可穿透并造成属性伤害
- 赤焰属性攻击（强度足够时）可解除冻结并转化为潮湿
- 冻结时生成寒冷云雾

#### 赤焰融冰

**触发**：赤焰攻击 + 目标被冻结（赤焰强度 ≥ 阈值）

- 立即解除冻结，清除灼烧，转化为潮湿状态（层数 = 冻结层数）

#### 冻结→蒸汽

**触发**：被冻结的实体获得灼烧状态（通过光环或赤焰攻击）

- 灼烧效果融化冻结，生成低温蒸汽云

#### 霜冻→潮湿

**触发**：灼烧 + 目标带有霜冻

- 霜冻层数转化为潮湿层数，同时清除灼烧和霜冻

#### 霜寒蒸汽云

**触发**：霜冻光环实体进入低温蒸汽云

- 蒸汽云变为霜寒状态，冻结云中的实体

#### 霜冻抑制孢子

**触发**：实体同时拥有霜冻和孢子效果

- 霜冻期间孢子效果失效，并加速孢子持续时间衰减
- 霜冻消失后，孢子恢复生效

#### 灼烧光环清除霜冻

**触发**：灼烧光环范围内有霜冻 / 临时霜冻目标

- 清除目标的霜冻状态，迫使逃跑

#### 冷凝

**触发**：处于低温蒸汽云中

- 缓慢叠加潮湿层数
- 感电 / 霜寒的蒸汽云阻止冷凝

### 通用反应

#### 热冲击

**触发**：处于灼烧状态的目标接触水

- 瞬间结算剩余灼烧伤害并清除灼烧状态
- 生成低温蒸汽云，并蒸发接触点的水

### 🩸 元素反制（濒血反击机制）

当携带元素属性的生物受到伤害、血量将跌破配置的**血线阈值**时，可能触发强力反制。所有反制共享同一框架：

- 实体的元素属性必须与反制属性匹配
- 实体对应属性的强化点数必须达到反制的强度阈值
- 触发后进入**血量回复式冷却**：实体必须将血量回复到配置的回复阈值以上，才能再次触发反制

#### 🔥 赤焰反制

**触发**：赤焰属性实体濒血（赤焰强化 ≥ 阈值；赤焰抗性低于灼烧免疫阈值）

- 自身原地锁定，期间**减免 90% 的所有类型受到伤害**
- 收缩的火环将附近实体拉向中心
- 随后爆炸：范围击退 + 赤焰伤害 + 对所有受影响实体**无条件施加灼烧**，伴随火龙卷视觉特效

#### 🌿 自然反制

**触发**：自然属性实体濒血（自然强化 ≥ 阈值）

- 爆发排斥波击退附近所有敌人，并对其施加孢子
- 根据配置可清除自身的灼烧状态

#### ⚡ 雷霆反制

**触发**：雷霆属性实体濒血（雷霆强化 ≥ 阈值）

- 召唤局部雷暴并扩张至最大半径：周期性落雷造成闪电伤害
- 施加满层麻痹和额外静电层数，并随时间对受影响实体强制施加潮湿

#### ❄️ 冰霜反制

**触发**：冰霜属性实体濒血（冰霜强化 ≥ 阈值）

- 以自身为中心生成冰霜爆发（冰暴），扩张至最大半径后驻留 40 tick
- 范围内敌人：施加霜冻（层数基于施法者冰霜强化点数），潮湿目标额外触发冻结
- 同时生成霜寒蒸汽云

所有反制参数（血线阈值、强度阈值、回复比例、半径、伤害）均可在配置文件中调整。

## 🌍 群系元素偏向

不同群系生成的生物会带有特定的元素倾向：

- 🔥 **炎热群系**（沙漠、恶地、下界等）→ 赤焰偏向
- ❄️ **寒冷群系**（雪原、冰刺平原等）→ 冰霜偏向
- 🌲 **森林 / 丛林群系** → 自然偏向
- ⛈️ **雷暴天气**（全局）→ 雷霆偏向

## 🧟 生物属性系统

生物在生成时按照以下优先级获取元素属性：

### 属性分配优先级

1. **黑名单**：处于属性黑名单中的生物不会获得任何属性
2. **强制属性（指令配置）**：若管理员通过指令（`/elementalcraft entity add`）为该生物类型配置了强制属性，则优先应用
3. **维度默认值**：若未配置强制属性：
   - **下界**：生物默认为赤焰属性（点数可配置）
   - **末地**：生物默认为雷霆属性（点数可配置）
4. **随机生成（概率）**：
   - **敌对生物**（Monster）有可配置的概率获得属性
   - **中立生物**（NeutralMob、猪灵）有独立的、较低的概率
   - 分配的元素由**群系元素偏向**决定（如沙漠 → 赤焰、雪原 → 冰霜）
   - 每个生物可获得：**攻击元素**（概率触发）、**强化点数**（分配到 4 个防具槽位）、**抗性点数**（分配到 4 个防具槽位）
   - 抗性类型有可配置的概率是攻击元素的**克制元素**（如火属性生物带冰霜抗性）

### 装备与耐久

拥有攻击属性的生物会获得：

- **武器**：一把**随机武器**（剑、斧、镐、锹等），附带对应的**元素攻击附魔**和**耐久 III**
- **已有手持武器**：直接在其武器上附加元素攻击附魔和耐久 III，不替换
- **防具**：防具会自动生成（若缺失），附带**强化**和/或**抗性附魔** + **耐久 III**，强化 / 抗性点数按比例分配到 4 个防具槽位

所有装备掉落概率默认设为 0%（不掉落装备本身）。属性数据会存入 NBT，用于附魔书掉落判定。

### 附魔书掉落

- 拥有元素属性的生物死亡时，有概率掉落**附魔书**（受抢夺等级影响）
- 附魔书可包含生物的：
  - **攻击附魔**
  - **强化附魔**（随机等级，基于强化点数）
  - **抗性附魔**（随机等级，基于抗性点数）
- 附魔书等级加权偏向生物的实际属性质量

### 维度默认值

- **下界**：所有生物默认为赤焰属性（可配置）
- **末地**：所有生物默认为雷霆属性（可配置）

### 生物逃跑行为

- 靠近拥有**灼烧光环**、**霜冻光环**或**静电光环**的实体时，生物会尝试**逃跑**（远离光环来源寻路，可通过配置选项关闭）
- **逃跑机制**：
  - 生物 AI 被暂时禁用（`setNoAi(true)`），并清除当前攻击目标
  - 生物计算远离光环来源的逃生路径，并尝试避开障碍物
  - 若卡住（5 tick 内位移小于 0.05 格），会尝试跳跃越过 1 格高的障碍物，或传送至有效的可站立方块位置
  - 逃跑持续最多 **200 tick（10 秒）**，到达目标点或远离光环源后停止
- **抑制**：若被 Iron's Spellbooks 的**根须缠绕（Root）**法术禁锢，则禁止逃跑

### 战术药水投掷（所有元素属性生物）

- **所有拥有攻击元素的生物**（赤焰、自然、雷霆、冰霜）且**不是 ISS 施法者**，都有可配置的概率在副手装备一瓶喷溅药水
- **药水类型取决于生物的元素属性**：
  - **赤焰属性生物**装备**喷溅剧毒药水**（用于触发灼烧的中毒增幅效果）
  - **自然、雷霆、冰霜属性生物**装备**喷溅水瓶**（用于施加潮湿，以触发自然寄生的潮湿加成、雷霆的麻痹或冰霜的冻结）
- **投掷行为**：
  - 当目标在 12 格内、有视线、尚未被所需效果影响且不是灼烧状态（对水瓶而言）时，生物向目标投掷药水
  - 投掷后等待 20 tick 判定结果：
    - **成功（效果生效）**：重置连续未命中计数
    - **失败（效果未生效）**：连续未命中 +1
  - 连续**未命中 3 次**后，生物放弃投掷（进入长冷却）
- 若配置 `wetnessNetherDimensionImmune` 为 true，下界生物不会装备水瓶（但剧毒药水可能仍会使用，取决于配置）

## 🛠️ 指令系统（管理员 / OP 专用）

所有指令均以 `/elementalcraft` 开头，支持 Tab 补全。

### 调试模式

```
/elementalcraft debug      # 切换调试模式：显示元素伤害计算过程和反应相关信息
```

### 重载配置

```
/elementalcraft reload     # 从磁盘重新加载所有配置缓存
```

### 群系偏向管理

```
/elementalcraft biome add <元素> <概率>
/elementalcraft biome remove <元素>
/elementalcraft biome list
```

### 强制实体属性

```
/elementalcraft entity add <攻击元素> [强化元素] [强化点数] [抗性元素] [抗性点数]
/elementalcraft entity remove        # 清除手持刷怪蛋对应的实体强制属性
```

- 示例：`/elementalcraft entity add fire fire 50 frost 20`
- 点数支持固定值（如 `50`）或范围值（如 `20-80`）
- 攻击元素之后的所有参数均为可选（默认：无、0、无、0）

### 实体属性黑名单

```
/elementalcraft entity blacklist add <元素>      # 手持刷怪蛋：禁止该实体携带指定元素（或填写 "all" 表示所有元素）
/elementalcraft entity blacklist remove <元素>
/elementalcraft entity blacklist list
```

### 强制物品属性

武器：

```
/elementalcraft item weapon add <元素>      # 绑定攻击属性（手持武器）
/elementalcraft item weapon remove          # 移除强制武器属性（手持武器）
```

防具：

```
/elementalcraft item armor add <强化元素> [强化点数] [抗性元素] [抗性点数]
/elementalcraft item armor remove           # 移除强制防具属性（手持防具）
```

- 强化元素为必填；点数、抗性元素、抗性点数为可选（默认：0、无、0）

### 效果免疫黑名单

```
/elementalcraft blacklist scorched add|remove|list    # 以灼烧为例
```

- 全部效果：scorched（灼烧）、spore（孢子）、static（静电）、paralysis（麻痹）、frostbite（霜冻）、freeze（冻结）、steam（蒸汽）、wetness（潮湿）

### 配置文件操作

- 通过指令做出的修改会自动保存并热加载，无需重启服务器
- 直接编辑 `config/ElementalCraft/` 目录下的 TOML 文件，模组每 100 刻会自动检测并刷新配置

## ✨ 视觉特效（按强化点数分级）

当装备的强化点数达到阈值时，会触发对应的视觉特效（阈值 = 配置文件中的 maxStatCap 值）：

### 近战挥砍特效

- **🔥 赤焰**：火焰弧光（高等级追加灵魂火、熔岩、烟雾效果）
- **🌿 自然**：堆肥粒子、孢子花（高等级追加樱花、蜡质粒子效果）
- **❄️ 冰霜**：冰符文粒子、雪花粒子（高等级追加霜冻覆盖层、冻结冰层、积雪层效果）
- **⚡ 雷霆**：发光粒子弧光（高等级追加反向传送门、闪电弧光效果）

### 远程投射物特效

- **🔥 赤焰**：双螺旋火焰轨迹（外层火焰、内层灵魂火，尾部附带熔岩粒子）
- **🌿 自然**：樱花螺旋轨迹（尾部附带村民喜悦粒子）
- **❄️ 冰霜**：雪花粒子螺旋轨迹（高等级追加霜冻覆盖层、冰符文粒子）
- **⚡ 雷霆**：闪电火花螺旋轨迹（高等级追加末地蜡烛、反向传送门、龙息效果）

### 撞击爆炸特效

- 对应元素粒子爆发（火焰/熔岩、樱花/孢子花、冰符文/雪花、发光粒子/末地蜡烛等）
- 特效强度随等级线性提升，最高等级效果极具视觉冲击力，可在 `elementalcraft-visuals.toml` 中调整

### 冰霜专属视觉效果

- **霜冻覆盖层**：实体带有霜冻效果时的屏幕覆盖层
- **霜冻积雪层**：冻结实体上的积雪渲染
- **冻结冰层**：冻结实体的冰壳视觉效果

## ⚙️ 配置文件（服务端 / 客户端）

配置文件位于 `config/ElementalCraft/` 目录下：

| 文件 | 用途 |
|------|------|
| **elementalcraft-common.toml** | 元素克制关系、伤害倍率、群系偏向、附魔加成、强制实体/黑名单、维度属性等 |
| **elementalcraft-forced-items.toml** | 强制物品属性配置（可通过指令添加） |
| **elementalcraft-fire-nature-reactions.toml** | 赤焰与自然元素反应参数（灼烧、孢子、蒸汽、毒火爆燃、赤焰/自然反制等） |
| **elementalcraft-thunder-frost-reactions.toml** | 雷霆与冰霜元素反应参数（静电、麻痹、霜冻、冻结、感电水域、雷霆/冰霜反制等） |
| **elementalcraft-visuals.toml** | 粒子特效开关、密度、角度、速度等 |

支持热加载：保存修改后配置会自动刷新。

## 🔗 Iron's Spellbooks 联动

当 **Iron's Spellbooks**（铁的法术书）与 Elemental : Reactions 同时安装时，将启用额外的联动功能。所有法术反应的触发概率和强度基于施法者的对应元素强化点数计算，并自动匹配最佳法术等级。

### 施法者生物生成

- 拥有攻击元素的生物在生成时，有概率成为 **ISS 法术施法者**
- 施法者生成概率由统一配置项 `casterMobChance` 控制（各元素共用此值）
- 施法者类型与攻击元素绑定（雷霆 → 雷霆施法者，自然 → 自然施法者，等）
- 施法者黑名单可阻止特定实体成为施法者
- **自然施法者**装备酸液球法术时，卷轴放在副手，额外获得一把随机武器（主手）
- **冰霜施法者**装备召唤北极熊法术时，卷轴放在副手，额外获得一把随机武器（主手）

### 赤焰法术反应

- 赤焰法术（火矢、火球术、烈焰冲刺、岩浆炸弹、烈焰弹幕、烈焰打击、灼烧、热浪、烈焰风暴、龙息术、火焰箭矢）命中时，根据施法者的**赤焰强化点数**概率触发**灼烧**效果，替换 ISS 自带的着火效果
- **烈焰风暴**（Blaze Storm）的每一发小型火球均可独立触发灼烧判定，但触发之间有内置冷却
- **烈焰打击**（Flaming Strike）和**地狱浮现**（Raise Hell）有专属处理器，通过法术反应路径触发灼烧
- **火墙 / 岩浆场**（Fire Field / Wall of Fire）持续对范围内实体施加灼烧，按以下顺序判定：
  - 先检查**赤焰融冰**（若目标被冻结）
  - 再检查**潮湿**（触发高温蒸汽云）
  - 最后施加灼烧
- 若目标同时有**中毒**或**易燃孢子**，灼烧触发概率提升至 **100%**，并增幅灼烧伤害

### 自然法术反应

- 自然法术（酸液球、毒箭、地震、萤火虫群、毒雾喷射、毒液溅射、根须缠绕、践踏等）命中时触发**自然寄生**（施加易燃孢子）
- **毒属性法术**（毒箭、毒雾喷射、毒液溅射）**不施加孢子**——仅施加原版中毒效果
- **酸液球**（Acid Orb）命中时触发 AoE 孢子施加（半径 3.5 格内所有实体）
- **根须缠绕**（Root）：
  - 命中时施加初始孢子层数
  - 缠绕持续期间，**持续**根据施法者自然强化点数追加孢子层数
  - 若被 Root 的目标受到**灼烧**，Root 被移除，同时灼烧持续时间和伤害倍率增强（与中毒增幅机制相同）

### 雷霆法术反应

- 闪电法术（闪电长矛、连锁闪电、球状闪电、电击、闪电束、冲击波、雷暴、升天、伏特打击）根据施法者的雷霆强化点数对目标施加**静电**层数
- 若目标有**潮湿**效果 → 触发**麻痹**（清除潮湿，转化为麻痹）
- 若目标**在水中** → 触发**感电水域**：按维度创建持久的感电区域，周期性伤害和麻痹水中的实体
- 若目标有**易燃孢子** → 触发**孢子引爆**
- 若目标在**低温蒸汽云**中 → 使蒸汽云**感电**，对云中实体施加伤害和麻痹
- **电击**（Electrocute）法术可**刷新麻痹**目标的麻痹持续时间；电击结束后，施法者进入麻痹冷却
- 原版**闪电束**击中潮湿目标时：若感电水域可用则触发感电；否则施加最大静电层数并立即解决潮湿冲突

### 冰霜法术反应

- 冰霜法术（寒冰锥、冰锥术、霜冻射线、霜浪、冰刺、雪球、霜咬、暴风雪、冰墓、召唤北极熊）根据施法者的冰霜强化点数概率触发**霜冻**效果
- **暴风雪**（Blizzard）AoE **每秒**对范围内所有实体施加霜冻判定
- **冰墓**（Ice Tomb）被生物施法者特殊处理——直接封锁目标 5 秒（不造成伤害，仅禁锢）
- 若目标已有**潮湿**效果 → 触发**冻结**（霜冻 + 潮湿 → 冻结）
- 冰霜法术命中已冻结的目标会**刷新冻结持续时间**，延长目标被禁锢的时间
- ISS 的 **Chilled** 效果与潮湿叠加时自动转化为**冻结**
- **北极熊**施法者会持续检测北极熊是否存活（48 格搜索范围），若死亡则重新召唤（最多 3 次）；施法者死亡时其召唤的北极熊自动移除

### 毒雾云 + 赤焰反应

- **灼烧目标**进入 ISS 的**毒雾云**范围时，毒雾云会增强灼烧的持续时间和伤害倍率（中毒增幅），然后消散（伴随粒子效果）
- 灼烧光环实体靠近毒雾云时也可触发此反应

### ROOT 法术联动

- **ROOT** 法术命中目标时，根据施法者的自然强化点数施加**易燃孢子**层数
- ROOT 持续期间，持续根据施法者自然强化点数追加孢子层数
- 若被 ROOT 的目标受到**灼烧**（赤焰攻击触发），ROOT 被移除，同时灼烧持续时间和伤害倍率增强（与中毒增幅机制相同）
- ROOT 禁锢期间抑制目标逃跑行为——受影响的生物停止逃跑尝试

### 生物施法 AI

所有 4 种元素的施法生物共享以下行为：

- 出生时自动装备对应元素的**法术卷轴**（主手 / 副手）和**元素攻击附魔**
- 法术等级基于生物的元素强化点数，通过稀有度匹配算法选取最合适的等级
- 生命值低于 **50%** 时进入**激进模式**（施法更频繁）
- 法术卷轴掉落概率可配置（默认：100%）
- **掉落附魔净化**：ISS 施法者掉落的物品若带有元素攻击附魔，会被自动清除（防止通过击杀生物无限获取元素附魔书）

#### 赤焰施法生物

- 副手装备**喷溅剧毒药水瓶**
- 战斗时先向目标投掷毒药水瓶，再施放法术：
  - 若目标潮湿，优先触发高温蒸汽云（消耗潮湿）
  - 潮湿清除后，中毒效果确保灼烧 **100% 触发**并增强持续时间
- 若毒药未命中多次，施法者会进入冷却

#### 自然施法生物

- **不投掷任何药水**
- 装备**酸液球**时——特殊 AI 循环：
  - 施法 → 等待命中判定 → 检测目标是否带有 **REND** 效果
  - 若 REND 生效：转为近战攻击，等待 REND 消失
  - 若 REND 未生效：重新尝试施法，连续 2 次未命中进入冷却
- 其他法术：常规施法，检测目标麻痹免疫状态

#### 雷霆施法生物

- 副手装备**喷溅水瓶**
- 战斗时先向目标投掷水瓶施加潮湿，再施放法术以触发麻痹
- 追踪目标潮湿状态，仅当目标未潮湿时才投掷水瓶
- 若目标仍未被潮湿，计为一次未命中；连续 2 次未命中进入冷却
- 若 `wetnessNetherDimensionImmune` 开启且在下界维度，不装备水瓶（潮湿效果已禁用）

#### 冰霜施法生物

- 副手装备**喷溅水瓶**（**召唤北极熊**施法者除外）
- 战斗时先向目标投掷水瓶施加潮湿，再施放法术以触发冻结
- 采用与雷霆施法者相同的命中追踪机制
- **召唤北极熊**施法者**不装备水瓶**：施法后持续检测北极熊是否存活（48 格搜索范围），若死亡则重新召唤（最多 3 次）；施法者死亡时自动移除其召唤的北极熊
- 若 `wetnessNetherDimensionImmune` 开启且在下界维度，不装备水瓶（潮湿效果已禁用）

### 其他联动细节

- **非攻击法术排除**：热浪、酸液球、橡木皮肤、龙息术、寒冰锥、电击等非攻击或持续施法法术**不会触发激进施法**
- **卷轴稀有度匹配**：生物根据自身强化点数计算法术最优等级（通过稀有度值匹配最接近的等级）
- **玩家法术追踪**：玩家最后一次施法的法术信息会被记录，用于后续命中的反应计算
- **雷霆法术附魔处理**：雷霆施法生物在伤害计算期间会临时处理目标物品附魔，确保伤害追踪准确

## 🔗 L_Ender's Cataclysm 灾厄联动

当安装 **L_Ender's Cataclysm**（灾厄）模组时，灾厄的潮湿效果会被 Elemental : Reactions 的潮湿系统取代：

- 来自灾厄的潮湿（例如潮湿类攻击命中）会变为 Elemental : Reactions 的潮湿，享受相同的叠加、自然衰减以及全部元素反应（蒸汽、麻痹、冻结、孢子等）
- 连续命中会逐层叠加潮湿，最高可叠满
- 保留灾厄的"潮湿 + 闪电"增伤机制：对潮湿目标，闪电会造成额外伤害（最高 +100%）
- 灾厄 Boss 也可以受到 Elemental : Reactions 效果的影响
- 未安装灾厄时，本模组不产生任何影响
