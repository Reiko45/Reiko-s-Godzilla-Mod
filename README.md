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

## What's new in Phase 3: MonsterVerse

### Titans

| Titan | Side | What it does |
|---|---|---|
| **Godzilla** (MonsterVerse) | Titan | Atomic breath, nuclear pulse, tail swipe. Feeds on radiation (see below). Explosives to his **gills** do double damage. His roar makes Titans freed from an Alpha Call bow to him |
| **Kong** (young or adult) | Titan | Climbs anything, leaps on enemies, grabs and throws them, beats his chest. Feed him fruit to win his trust. A young Kong from Skull Island grows up in time |
| **Suko** | Titan | Young Great Ape: boulder throws and leaps. Goes straight for the Skar King's crystal. Tame him with fruit |
| **Skar King** | Invader | Whipslash (long reach, drags you in), freezing blasts from his crystal, pounce, throws. Calls his Great Ape army when hurt |
| **Shimo** | Titan / Invader | Frost Bite Blast leaves ice where it lands; water and lava freeze as she walks. Can't be frozen; fire and atomic breath hurt her more |
| **MUTOs** (male and female) | Invader | The male flies and fires **EMP** pulses; the female walks and carries a permanent EMP field. Both hunt radiation, and a fed female lays a nest |
| **King Ghidorah** (MonsterVerse) | Invader | Gravity beams from each head, wingtip lightning, a storm wherever he goes, and the **Alpha Call** |
| **Rodan** (MonsterVerse) | Titan | Rains lava, and a supersonic low pass shatters glass and strips trees. Follows the Alpha |
| **Mothra** (MonsterVerse) | Guardian | God rays, silk webbing, stingers, scale shield. Breaks up fights between Godzilla and Kong |
| **Scylla** | Invader | Spider-like climber: impaling legs, tendril grab, scuttle charge. Hunts radiation, follows the Alpha |
| **Behemoth** | Titan | Tusk slam that stuns everything nearby; regrows the forest around him. Follows the Alpha |
| **Methuselah** | Titan | Sleeps disguised as a mountain until the Alpha Call, the ORCA or a player wakes him. Mountain charge, quake stomp |
| **Tiamat** | Invader | Sea serpent: fast in water, weak on land. Electric coils, blinding ink, razor scales that cut attackers |
| **Mechagodzilla** (Apex) | Human / Invader | Pilotable (see below). Proton Scream, missiles, rocket fist, drill tail, thrusters |
| **Skullcrawlers** and the **Skull Devil** | Invader | Pack hunters from under Skull Island. The Skull Devil's mouth takes double damage while its tongue is out |
| **Warbats** | Invader | Hollow Earth flyers that snatch prey, carry it up and drop it. They tear aircraft apart |
| **Great Apes** | Invader / Titan | The Skar King's tribe. They're free (and only defend themselves) once he dies |

### MonsterVerse systems

- **Godzilla's forms.** Godzilla absorbs **Nuclear Warheads** (and other radiation sources) within reach, and goes looking for them when he's hungry. A full charge makes him **Energized**: bright blue breath, faster healing and a spiral breath for 5 minutes. Feeding him more while Energized pushes him **thermonuclear**: a red-orange glow, a scorching aura and repeated pulses for a minute. So does Mothra dying near him. When he absorbs Tiamat's **Solar Crystals**, or Tiamat dies near him, he becomes **Evolved** for good: magenta breath and more health.
- **Alpha Call.** MonsterVerse King Ghidorah's call wakes every Titan that follows the Alpha (Rodan, Behemoth, Methuselah, Scylla) within 512 blocks. They turn on everyone and fight alongside him. When he dies they bow. Godzilla's roar also frees any of them whose Alpha is far away.
- **EMP.** MUTO pulses knock out mechs, Maser Tanks and the HEAV for several seconds: no weapons, no movement, and aircraft fall. Players gliding with elytra drop out of the air.
- **Kong's Battle Axe** (use it on Kong) soaks up beams aimed at him from the front, then unloads the energy in one explosive blow, twice as strong against Mechagodzilla. **Frostbite** from Shimo freezes it useless. The **B.E.A.S.T. Glove** (use it on Kong) cures frostbite, protects him from cold and electrifies his punches.
- **The Skar King's crystal** controls any Shimo nearby. Hit the crystal on his whip (Suko is especially good at it) to shatter it, and Shimo turns on him.
- **Apex Mechagodzilla** runs at about 40% power on its own. Use a **Hollow Earth Energy Core** on it for full power. At full power there's a chance in every fight that King Ghidorah's mind takes over: the pilot is thrown out and it becomes a rogue boss. A **Ghidorah Skull** (torn from MonsterVerse Ghidorah's head) is needed to build it.
- **Frostbite** (status effect): slower movement, weaker blows and slower mining. Stand in fire or lava to cure it.

### Tech

| Item | Use |
|---|---|
| **HEAV** | Flying vehicle. The only safe way through a vortex. R: sonar scan (marks every Titan within 192 blocks). V: decoy flares |
| **ORCA** | Use: on/off (or power it with redstone). Sneak + use: change signal. **Calm** makes Titans drop their fights, **Lure** draws them to it, **Alpha** wakes sleeping Titans and enrages the ones nearby |
| **Nuclear Warhead** | Inert. Godzilla, MUTOs and Scylla smell it from far away |
| **Mechagodzilla (Apex)** | R: Proton Scream (missiles while it recharges). V: rocket fist. G: thruster leap |

| Recipe | Pattern |
|---|---|
| HEAV | `PEP` / `SCS` / `IRI`: phantom membranes, elytra, shulker shells, recovery compass, iron blocks, redstone block |
| Mechagodzilla (Apex) | `TKT` / `NSN` / `M M`: Space Titanium, Ghidorah Skull, netherite blocks, nether star, Maser Tanks |
| Kong's Battle Axe | `DDN` / `DS ` / ` S `: Godzilla Dorsal Plates, netherite ingot, sticks |
| B.E.A.S.T. Glove | `TLT` / `TNT` / `RCR`: Space Titanium, lightning rod, netherite ingot, redstone blocks, copper block |
| ORCA | `NAN` / `RJR` / `IGI`: note blocks, amethyst shard, redstone, jukebox, iron ingots, G-Cell |
| Nuclear Warhead | `IGI` / `TCT` / `IGI`: iron blocks, glowstone, TNT, G-Cell |

Both Godzillas drop **Dorsal Plates**. Kong's throne room always has an axe and an energy core.

### The Hollow Earth

A new dimension: a vast cavern world lit by glowstone, with three biomes. The **Hollow Earth Jungle** has jungle trees, vines and Great Apes. The **Crystal Plains** are full of amethyst. The **Subterranean Depths** are lava, basalt and blackstone.

- **Getting in.** Step or fly into a **vortex**: the Antarctic vortices in snowy biomes, or the shaft on Skull Island. You arrive at the same coordinates, and a return vortex opens beside you. Without a HEAV the gravity flip hurts and leaves you dizzy and blind for a moment.
- **Kong's Throne Room**: a temple in the jungle with a giant throne, Kong himself, and a chest holding the **Battle Axe** and a **Hollow Earth Energy Core**.
- **The Skar King's Realm**: a lava-lit cavern in the Subterranean Depths where Shimo is chained and the Skar King rules with his apes.
- **Return vortices** are scattered through the Hollow Earth.

### Overworld

- **Skull Island** (warm oceans): jungle, a skull-faced mountain with Skullcrawler caves in its eye sockets (the Skull Devil lurks in one), the Iwi wall, a young Kong, a shaft down to a Hollow Earth vortex, and a **storm wall** of lightning that batters aircraft trying to cross it.
- **Tiamat's Lair** (frozen oceans): an ice dome on the sea floor with Tiamat and a cluster of Solar Crystals.
- **Antarctic vortices** (snowy biomes), **Methuselah** asleep in the hills, and **Behemoth** in the jungle.
- **New events**: Godzilla on the move, a MUTO pair (a female bursts from the ground and her mate flies in), Scylla, Behemoth, the Alpha Call (Ghidorah and a storm arrive, and a Rodan answers), and Skullcrawler swarms.

## Commands

`/kaiju event <godzilla|ghidorah|mechagodzilla|spacegodzilla|battra|destoroyah|monsterverse_godzilla|muto|scylla|behemoth|alpha_call|skullcrawlers>` starts an event near you (needs operator permissions).

## Config

`config/reikogodzilla-common.toml`:

- `kaiju.scale`: size of every kaiju (1.0 = one fifth of film size).
- `kaiju.godzillaHostileToPlayers`, `kaiju.bossBars`, `kaiju.playerDamageMultiplier`, `kaiju.mothraGrowthTicks`
- `destruction.kaijuGriefing`, `beamBlockDamage`, `beamFire`, `maxBlocksPerTick`, `maxBlockResistance` (block breaking also respects the `mobGriefing` game rule)
- `events.enabled`, `firstDay`, `intervalTicks`, `chance`, `maxKaijuNearPlayer`
- `weapons.maserAutoFire`, `maserDamage`, `oxygenDestroyerKaijuDamage`
- `heisei.burningGodzilla`, `meltdownTicks`, `meltdownExplosion`, `frozenTicks`, `juniorGrowthTicks`, `destoroyahChance`, `biollanteGrowthStages`
- `monsterverse.alphaCall`, `kongGrowthTicks`, `apexTakeoverChance`, `iceAge`, `orcaRange`, `vortexDamage`

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

- Phase 3: `godzilla_mv`, `godzilla_evolved`, `kong`, `suko`, `great_ape`, `skar_king`, `shimo`, `muto_male`, `muto_female`, `skullcrawler`, `skull_devil`, `warbat`, `king_ghidorah_mv`, `scylla`, `behemoth`, `methuselah`, `tiamat`, `apex_mechagodzilla`, `heav`

Fire Rodan uses the `rodan` model with its own texture, `rodan_fire.png`. MonsterVerse Rodan and Mothra reuse the `rodan` and `mothra` models with the textures `rodan_mv.png` and `mothra_mv.png`. Burning Godzilla tints `godzilla_heisei` orange.

**Bones:** a bone named `head` turns to follow where the kaiju is looking. A few bones are shown or hidden by the game: `axe` and `glove` on Kong (when he has them), `crystal` on the Skar King (until it shatters), and `head_0`, `head_1`, `head_2` on MonsterVerse Ghidorah (while a head is severed). Everything else is up to you.

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
| `leap`, `throw` | Leaping attacks (Kong, the Skar King, Suko, Apex Mechagodzilla's thrusters) and grab-and-throw |
| `slam` | Ground-pound attacks (Behemoth, Methuselah, Shimo, the female MUTO) |
| `emp`, `ink` | The male MUTO's EMP pulse, Tiamat's ink cloud |
| `dormant` | Methuselah asleep as a mountain, Shimo in chains |
| `swim` | Tiamat in water |

Beam start points (mouths, Ghidorah's three heads, Mechagodzilla's eyes, weapon mounts on the mechs) and extra hitboxes are set in each kaiju's class (`beamLocalOffset` and `createParts`), measured in blocks at scale 1 with +z forward. Adjust them if your model's proportions differ.

## Sounds

Sounds are currently re-pitched vanilla sounds. To use your own, put `.ogg` files in `src/main/resources/assets/reikogodzilla/sounds/` and point the entries in `assets/reikogodzilla/sounds.json` at them (for example `"reikogodzilla:godzilla_roar"`).

## Building

```
./gradlew build
```

The mod jar ends up in `build/libs/`. Use `./gradlew runClient` to launch a test game.

Mechs use a small client-side mixin (`CameraMixin`) for the over-the-shoulder pilot camera.
