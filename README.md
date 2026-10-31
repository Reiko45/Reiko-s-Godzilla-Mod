# Reiko's Godzilla Mod

Giant monsters from every era of Godzilla films, for **Minecraft 1.21.1 (NeoForge)**.

Kaiju fight each other, level terrain with charged beam attacks, and can be fought with film-inspired human tech like the Maser Tank and the Oxygen Destroyer. The mod is built in five phases (Showa/Heisei icons → Heisei → MonsterVerse → Showa & Millennium deep cuts → Reiwa & modern films). See **[ROADMAP.md](ROADMAP.md)**.

> Unofficial fan project. Godzilla and related characters are trademarks of Toho Co., Ltd.; MonsterVerse designs belong to Legendary. This mod contains no ripped models, logos or film audio.

![Atomic breath and gravity beams in a test fight](docs/beams_test.png)

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.x
- [GeckoLib](https://www.curseforge.com/minecraft/mc-mods/geckolib) 4.9.3 or newer for NeoForge 1.21.1

## What's in Phase 1

### Monsters

| Kaiju | Side | Size (default scale) | Abilities |
|---|---|---|---|
| **Godzilla** (Showa / Heisei) | Titan | ~10 / ~20 blocks | Atomic breath (dorsal plates charge up first), tail swipe, roar, nuclear pulse (Heisei), fast regeneration |
| **King Ghidorah** | Invader | ~13 blocks, flies | Three gravity beams (each head can aim at a different enemy), hurricane wing gusts, calls lightning |
| **Mothra** (larva → cocoon → imago) | Guardian | larva ~6 long, imago ~18 wingspan | Larva: silk spray. Imago: poison scale clouds, hurricane gusts. Defends players from hostile kaiju |
| **Rodan** | Titan | ~5 blocks, ~18 wingspan | Supersonic dive with a sonic boom, wing gusts |
| **Anguirus** | Titan | ~12 long | Spiked shell (thorns), Thunderball roll. Tameable and rideable |
| **Mechagodzilla** (Showa) | Invader | ~10 blocks | Rainbow Space Beam, finger missiles, spinning-head force field, "Fake Godzilla" disguise |

**Sides:** Titans and Guardians leave players alone unless attacked, and fight Invaders. Invaders attack everyone. Godzilla can be made hostile to players in the config (1954 behaviour).

Kaiju have boss bars, extra hitboxes for heads, tails and wings (head hits do more damage), wade along the sea floor, and smash through anything in their path.

### Tech and items

- **Type 66 Maser Tank**: place it, use it to climb in. W/S drive, A/D turn, **R** fires the maser where you look. Unmanned tanks guard the area and shoot hostile kaiju. Sneak + use packs it up; iron ingots repair it.
- **Oxygen Destroyer**: throw it into water. Every living thing in the water nearby dies, kaiju lose most of their health and can't regenerate for two minutes.
- **Kaiju drops**: G-Cells (Godzilla), Space Titanium (Mechagodzilla), Ghidorah Scales, Mothra Scales, Kaiju Scales. These feed into later phases' tech.

| Recipe | Pattern |
|---|---|
| Oxygen Destroyer | ` G ` / `PHP` / `IDI`: glass, prismarine crystals, heart of the sea, iron ingots, diamond |
| Maser Tank | ` D ` / `RIR` / `ICI`: diamond, redstone blocks, iron blocks, minecart |

### World

- **Monster Island** (open ocean): a volcanic island. Godzilla rests just offshore, Rodan nests in the lava-filled crater and Anguirus roams the plateau. They appear when you get close.
- **Infant Island** (warm oceans): jungle island with a stone shrine holding **Mothra's Egg**. While you're nearby the egg cracks and hatches a larva (sometimes twins). Larvae spin a cocoon after a while and emerge as Mothra.
- **Kaiju events** (from day 5, roughly every few days): Godzilla surfaces offshore and heads inland, a golden meteor crashes and King Ghidorah climbs out, or "Godzilla" walks in from the horizon... and turns out to be Mechagodzilla.

![Monster Island and Infant Island, top-down](docs/islands_map.png)

### Taming Anguirus

Feed it any meat until hearts appear. Then: sneak + use to make it sit or follow, use a saddle on it, and use it again to ride. While riding, press **R** to roll in the direction you're looking. It defends you and attacks whatever you attack.

## What's new in Phase 2: Heisei

### Monsters

| Kaiju | Side | Abilities |
|---|---|---|
| **Godzilla** (Heisei) | Titan | Gains the **Red Spiral Ray** for a while when a Fire Rodan dies nearby. Badly hurt, he becomes **Burning Godzilla** (see below) |
| **Fire Rodan** | Titan | Half of all Rodan. Uranium Heat Beam |
| **Baby → Little → Godzilla Junior** | Titan | Hatches from the **Godzilla Egg** in a nest on Monster Island and imprints on the nearest player. Feed it G-Cells to grow it a stage, meat to heal it; sneak + use to make it sit or follow. Bubble breath, later atomic breath |
| **Biollante** (Flower → Plant Beast) | Titan | Vine lash, vine grab (pulls you in), acid sap, spore clouds. Killing the Flower Beast brings her back as the Plant Beast |
| **Battra** (larva → cocoon → imago) | Titan | Larva: Prism Beam, horn ram. Imago flies: Prism Beam, horn bolts, wing gusts. Turns to fight alongside Mothra once she reaches it while it's badly hurt |
| **SpaceGodzilla** | Invader | Corona Beam (curves toward its target), Gravity Tornado, Photon Reactive Shield (reflects beams), tail swipe. Grows Space Crystal fields that feed it energy: break the crystals to stop it regenerating |
| **Destoroyah** (juveniles → aggregate → flying → Perfect Form) | Invader | Micro-Oxygen beam, Variable Slicer, tail swipe, roar. Five juveniles merge into the aggregate, which evolves as it fights; the Perfect Form can split into a swarm and reform once. Weak to freezing |

**Burning Godzilla:** when Heisei Godzilla drops low, his heart starts melting down. He glows orange, scorches everything around him and gains the Infinite Heat Ray, while a heat meter climbs. Freezer weapons cool him down. If the meter fills he explodes; if a Godzilla Junior is nearby it absorbs the radiation and becomes the new Godzilla instead.

**Weaknesses:**
- **Freeze meter:** freezer weapons fill it. A full meter freezes a kaiju solid for a few seconds, and frozen kaiju take 50% more damage.
- **Cadmium:** cadmium missiles stop regeneration and nuclear attacks (atomic breath, heat rays) for a while.
- **Beam reflection:** the Super X2's Fire Mirror and SpaceGodzilla's shield bounce beams back at whoever fired them. G-Force Mechagodzilla soaks up beams to charge its Plasma Grenade.

### Mechs and vehicles

Craft them, place them, and use them to climb in. Pilots are protected and can't be seen inside. Sneak + use packs a machine back into an item; iron blocks repair mechs.

| Machine | R | V | G |
|---|---|---|---|
| **Mechagodzilla (G-Force)** | Mega-Buster (missiles while it recharges) | Shock Anchors, or Plasma Grenade once it's charged | Undock Garuda |
| **Garuda** (flies) | Masers | | Dock onto a Mechagodzilla within 24 blocks: **Super Mechagodzilla** (flies, more armour, Garuda masers) |
| **MOGUERA** | Plasma Laser | Spiral Grenade Missiles, or drill charge | Split into Star Falcon + Land Moguera |
| **Star Falcon** (flies) | Plasma Laser | Missiles | Recombine with Land Moguera |
| **Land Moguera** | Drill charge | | |
| **Super X** (flies) | Cadmium Missiles | Lasers | |
| **Super X2** (flies) | Torpedo Missiles | Fire Mirror: reflects beams. It melts after 5 hits; repair it with a diamond block | |
| **Super X3** (flies) | Cryo Laser | Freezer Missiles | |
| **DAG-MB96 Freezer Tank** | Freezer beam | | |

Unmanned mechs and tanks defend you: they attack hostile kaiju and anything targeting a player.

**Piloting:** W/S to walk or fly, steer with the mouse. Flying machines take off when you look up while moving forward and land when you look down at the ground. In third person the camera moves over the machine's shoulder, and the crosshair shows exactly where your weapons will hit. Keys can be changed under *Controls → Reiko's Godzilla Mod*.

| Recipe | Pattern |
|---|---|
| G-Cell Rose (shapeless) | rose bush + G-Cell + bone meal |
| Freezer Tank | ` B ` / `RIR` / `ICI`: blue ice, redstone blocks, iron blocks, minecart |
| Super X | `IKI` / `RER` / `IKI`: iron blocks, Kaiju Scales, redstone blocks, elytra |
| Super X2 | `KDK` / `DXD` / `KRK`: Kaiju Scales, diamond blocks, Super X, redstone block |
| Super X3 | `SBS` / `BXB` / `SNS`: Space Titanium, blue ice, Super X2, netherite ingots |
| Garuda | `S S` / `SES` / `RMR`: Space Titanium, elytra, redstone blocks, Maser Tank |
| Mechagodzilla (G-Force) | `SGS` / `SNS` / `T T`: Space Titanium, Ghidorah Scale, netherite ingot, Maser Tanks |
| MOGUERA | `SBS` / `SNS` / `D D`: Space Titanium, beacon, netherite ingot, diamond pickaxes |

### World

- **G-Cell Rose:** plant it on soil within 4 blocks of water. While you're nearby it grows... and becomes Biollante.
- **Godzilla Egg:** in a nest on Monster Island. It hatches when you're close.
- **New events:** SpaceGodzilla falls from space inside a crystal meteor, the ground splits open and a Battra larva bursts out, or a swarm of Destoroyah juveniles crawls out of the water. Using an Oxygen Destroyer can also awaken Destoroyah juveniles a few minutes later.

## Commands

`/kaiju event <godzilla|ghidorah|mechagodzilla|spacegodzilla|battra|destoroyah>` starts an event near you (needs operator permissions).

## Config

`config/reikogodzilla-common.toml`:

- `kaiju.scale`: size of every kaiju (1.0 = one fifth of film size).
- `kaiju.godzillaHostileToPlayers`, `kaiju.bossBars`, `kaiju.playerDamageMultiplier`, `kaiju.mothraGrowthTicks`
- `destruction.kaijuGriefing`, `beamBlockDamage`, `beamFire`, `maxBlocksPerTick`, `maxBlockResistance` (block breaking also respects the `mobGriefing` game rule)
- `events.enabled`, `firstDay`, `intervalTicks`, `chance`, `maxKaijuNearPlayer`
- `weapons.maserAutoFire`, `maserDamage`, `oxygenDestroyerKaijuDamage`
- `heisei.burningGodzilla`, `meltdownTicks`, `meltdownExplosion`, `frozenTicks`, `juniorGrowthTicks`, `destoroyahChance`, `biollanteGrowthStages`

## Custom models (Blockbench)

Every kaiju currently uses a plain **stand-in model** so the mod is playable. To use your own, make a GeckoLib model in Blockbench (*GeckoLib Animated Model* project type, entity) and export three files with the same name, replacing the stand-ins:

```
src/main/resources/assets/reikogodzilla/geo/entity/<name>.geo.json
src/main/resources/assets/reikogodzilla/animations/entity/<name>.animation.json
src/main/resources/assets/reikogodzilla/textures/entity/<name>.png
```

Names:

- Phase 1: `godzilla_showa`, `godzilla_heisei`, `king_ghidorah`, `mothra_larva`, `mothra_cocoon`, `mothra`, `rodan`, `anguirus`, `mechagodzilla`, `maser_tank`
- Phase 2: `godzilla_junior`, `biollante_flower`, `biollante_plant`, `battra_larva`, `battra`, `space_godzilla`, `space_crystal`, `destoroyah_juvenile`, `destoroyah_aggregate`, `destoroyah_flying`, `destoroyah_perfect`, `mechagodzilla_heisei`, `super_mechagodzilla`, `garuda`, `moguera`, `star_falcon`, `land_moguera`, `super_x`, `super_x2`, `super_x3`, `freezer_tank`

Fire Rodan uses the `rodan` model with its own texture, `rodan_fire.png`. Burning Godzilla tints `godzilla_heisei` orange.

**Bones:** a bone named `head` turns to follow where the kaiju is looking. Everything else is up to you.

**Animations** the code plays. Use these exact names; if one is missing, that move simply won't animate:

| Animation | When |
|---|---|
| `idle`, `walk`, `fly` | Always (fly only for flying kaiju) |
| `attack` | Melee strike (played once) |
| `roar` | Roar ability and triggered roars |
| `charge` | Wind-up before most special attacks (atomic breath charge-up, etc.) |
| `beam` | While a beam is firing |
| `tail` | Godzilla's tail swipe |
| `pulse` | Heisei Godzilla's nuclear pulse |
| `gust` | Wing gust (Ghidorah, Mothra, Rodan) |
| `scales` | Mothra's poison scales |
| `spray` | Mothra larva's silk spray |
| `dive` | Rodan's sonic dive |
| `curl`, `roll` | Anguirus's Thunderball |
| `sit` | Tamed Anguirus sitting |
| `missiles`, `shield` | Missile barrages; force fields, the Fire Mirror and SpaceGodzilla's shield |
| `cocoon` | Mothra's and Battra's cocoons |
| `lash`, `grab`, `spores` | Biollante's vine lash, vine grab and spore burst |
| `ram` | Battra larva's horn ram |
| `tornado` | SpaceGodzilla's Gravity Tornado |
| `drill` | MOGUERA's and Land Moguera's drill charge |

Beam start points (mouths, Ghidorah's three heads, Mechagodzilla's eyes, weapon mounts on the mechs) and extra hitboxes are set in each kaiju's class (`beamLocalOffset` and `createParts`), measured in blocks at scale 1 with +z forward. Adjust them if your model's proportions differ.

## Sounds

Sounds are currently re-pitched vanilla sounds. To use your own, put `.ogg` files in `src/main/resources/assets/reikogodzilla/sounds/` and point the entries in `assets/reikogodzilla/sounds.json` at them (for example `"reikogodzilla:godzilla_roar"`).

## Building

```
./gradlew build
```

The mod jar ends up in `build/libs/`. Use `./gradlew runClient` to launch a test game.

Mechs use a small client-side mixin (`CameraMixin`) for the over-the-shoulder pilot camera.
