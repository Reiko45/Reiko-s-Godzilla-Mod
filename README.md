# Reiko's Godzilla Mod

Giant monsters from every era of Godzilla films, for **Minecraft 1.21.1 (NeoForge)**.

Kaiju fight each other, level terrain with charged beam attacks, and can be fought with film-inspired human tech like the Maser Tank and the Oxygen Destroyer. The mod is built in five phases (Showa/Heisei icons → Heisei → MonsterVerse → Showa & Millennium deep cuts → Reiwa & modern films); all five are done. See **[ROADMAP.md](ROADMAP.md)**.

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

## What's new in Phase 4: Showa and Millennium

### Showa monsters

| Monster | Side | What it does |
|---|---|---|
| **Gigan** (Showa / Final Wars) | Invader | Hammer hands, a belly buzzsaw that holds its victim, a visor laser and dive attacks. The Final Wars Gigan is twice the size, reels prey in with grappling scythes and fires the Gigarium Cluster beam, which bursts into explosive bolts. Beaten to half health he is rebuilt as **Modified Gigan**, with chainsaws and homing **Bladed Slicer** discs |
| **Hedorah** | Invader | Grows through four stages by feeding on pollution: lit furnaces, smokers, campfires, fire, lava, magma (the Nether is a feast). Tadpole → land form → flying saucer that rains **acid mist** (rusts armour, kills plants) → the towering Perfect form with the Hedrium Ray. Big hits splash off sludge that crawls back to heal it. Nearly beaten, it turns back into the saucer to escape |
| **Megalon** | Invader | Drill arms, the Double Drill (tunnels under you and bursts out), napalm, horn lightning, grasshopper leaps and a ring of fire. Without Seatopia's guidance he only notices what's right in front of him |
| **Jet Jaguar** | Human | Your robot: place it from its item. Follows you at human size and grows giant to fight hostile kaiju (or use the controller). Flying kicks and throws. Left far from you, Seatopia can hijack it; once it breaks free it becomes self-aware |
| **Ebirah** (Showa / Final Wars) | Invader | Giant lobster of Letchi Island: a scissor claw that crushes, a spear claw that pierces armour, and he wrecks boats. Clumsy on land, always heads back to sea |
| **Kumonga** (Showa / Final Wars) | Invader | Giant spider: sticky thread that binds and leaves cobwebs, a venom needle, ambushes from underground. Fire does double damage |
| **Kamacuras** (Showa / Final Wars) | Invader | Giant mantises that hunt in threes, slash, pounce and fly. They burn very easily (triple fire damage) |
| **Minilla** | Titan | Godzilla's son. Hatches from an egg kept warm by heat underneath and attaches to the nearest player. Blows smoke rings until he's practised enough for real atomic breath (practice next to a grown Godzilla counts triple). Feed a wild one fish to tame it |
| **Gorosaurus** | Titan | Allosaurus from Monster Island with the famous kangaroo kick |
| **Manda** (Showa / Final Wars) | Titan / Invader | Sea dragon that coils around its enemy, even a battleship, and crushes it. Freezes twice as fast and shatters when frozen |
| **Baragon** (Showa / GMK) | Titan / Guardian | Burrows, leaps; the Showa version fires a magma ray and hunts livestock at night. GMK Baragon is a Guardian Monster (see below) |
| **Varan** | Titan | Glides on skin membranes and swims. Can't resist light (see Light Bombs) |
| **King Caesar** (Showa / Final Wars) | Guardian | His **Prism Eye** catches beams and fires them back twice as strong for a few seconds, then needs to recover. Nimble kicks and throws. Sleeps in his cliff until woken (see below) |
| **Titanosaurus** | Titan | Tail-fan gales that blow kaiju away. Dr. Mafune's implant in his head lets the Black Hole aliens control him; smash the implant (head hits) to free him for good |
| **Mechagodzilla 2** | Invader | The rebuilt Showa Mechagodzilla: rotating finger missiles, a tougher hull. It relays the Black Hole aliens' orders to Titanosaurus, and if its own link is cut after it's been linked, it shuts down |

### Millennium monsters and machines

| Monster | Side | What it does |
|---|---|---|
| **Godzilla 2000** | Titan | Red-orange breath and the Organizer G1: heals extraordinarily fast |
| **GMK Godzilla** | Invader | Possessed by the vengeful dead, always hostile, extremely tough. At half health (or bitten by GMK Ghidorah) a **wound** opens in his shoulder: D-03 missiles fired into it do enormous damage |
| **Godzilla (2002)** | Titan | The Godzilla whose roar can wake the first Godzilla's spirit inside Kiryu |
| **Final Wars Godzilla** | Titan | 100 m, fast and agile; his breath becomes the red Hyper Spiral Burning when he's pushed to the limit |
| **Millennian UFO** → **Orga** | Invader | The UFO fires a plasma cannon, binds with cables and sends out shockwaves. It absorbs half of any Godzilla's breath and studies it: destroy it after it has absorbed enough and the alien crawls out as **Orga**. Orga regenerates fast, fires a shoulder cannon and swallows enemies whole. Break the shoulder cannon to cripple its healing; swallowing a Godzilla is fatal for Orga |
| **Megaguirus**, **Meganulon**, **Meganula** | Invader | Meganulon larvae hatch from egg masses in water and molt into Meganula, which swarm kaiju and drain their energy. Full Meganula fly home and feed the egg mass; feed it five times and **Megaguirus** bursts out. She flies very fast, drains with her stinger (then fires the stolen energy as a beam) and sends out ultrasonic waves that knock out machines. Break her stinger by hitting her tail |
| **GMK Guardians**: Baragon, Mothra, King Ghidorah | Guardian | Rise from their shrines when GMK Godzilla comes near. Break a shrine and its Guardian hunts *you*. When Mothra dies, King Ghidorah takes her energy and becomes the **Thousand-Year-Old Dragon King** (flight, gravity beams, a barrier). Each fallen Guardian strengthens the others |
| **Kiryu** / **Kiryu Kai** | Human | Pilotable (see below) |
| **Monster X** → **Keizer Ghidorah** | Invader | The Xiliens' trump card: three gravity beams, levitating martial arts. At half health he sheds his shell and becomes Keizer Ghidorah: anti-gravity beams that lift and hurl, a draining bite, a barrier |
| **Zilla** | Invader | Blindingly fast, leaps away from half the beams aimed at it. Fragile |
| **Xilien saucers** | Invader | Small laser-armed craft. Each carries a Xilien control relay (see below): shoot them down to free their monsters |
| **Gotengo** | Human | Pilotable flying battleship (see below) |

### Alien control

Several films end with an alien controller destroyed and its monsters freed, so it works the same way here. **Alien Control Transmitters** (and Xilien saucers, and Mechagodzilla 2) broadcast a signal; every monster the signal can reach fights for the aliens (red boss bar, hostile to everyone, coloured sparks over its head). Break the transmitter or kill the carrier and they're freed: confused for ten seconds, then back to their own side.

| Signal | Controls |
|---|---|
| Nebula M (the Godzilla Tower) | Gigan, Showa King Ghidorah |
| Seatopia | Megalon; Jet Jaguar if its owner is far away |
| Kilaak (Destroy All Monsters) | Showa Godzilla and the Monster Island residents: Rodan, Anguirus, Minilla, Gorosaurus, Manda, Baragon, Varan, Kumonga, Kamacuras |
| Black Hole Planet 3 | Titanosaurus (with his implant), Mechagodzilla |
| Xilien | Every monster with **M-Base**: the Final Wars monsters, Monster X, Zilla, Rodan, Anguirus. Godzilla doesn't have it |

Tamed and piloted kaiju can't be taken over.

### Weaknesses from the films

- **Electrode Plates** (Hedorah): power one with redstone and it arcs at any Hedorah within 24 blocks, drying it out: no healing, double damage, and it crumbles. Hedorah also takes extra damage and never heals in hot, dry places; water and rain heal it.
- **Yellow Juice** (Ebirah): drink it and Ebirah leaves you and your boat alone for five minutes. The Red Bamboo's chests also hold a fake batch.
- **Light Bombs** (Varan): throw one and it burns as a flare for fifteen seconds. Varan can't resist it, swallows it, and the charge goes off inside him.
- **Supersonic Wave Oscillator** (Titanosaurus): a beam of supersonic waves along your view stuns him for seven seconds (other kaiju only stagger).
- **D-03 Missiles** (GMK Godzilla): fire along your view; into the open wound they do 8-14x damage.
- **Freezing** (Manda): freezes twice as fast and takes double damage while frozen.
- **Fire** (Kumonga, Kamacuras): double and triple damage.

### Machines and tech

| Item | Use |
|---|---|
| **Kiryu** | R: twin masers. V: back-unit missiles / Type-0 railguns. G: the **Absolute Zero Cannon** (freezes its target solid, costs 40% power) or a thruster leap. Everything costs power; at zero he shuts down. Recharge with a **White Heron Power Cell** or park him by a **Kiryu Maintenance Gantry**. When a Godzilla roars nearby, the first Godzilla's spirit may wake: Kiryu throws you out and rampages until his batteries are flat |
| **Kiryu Kai Upgrade Kit** | Use on Kiryu: the Triple Hyper Maser (R) replaces the twin masers and the Absolute Zero Cannon, and G becomes the Spiral Claw drill, which bores through terrain |
| **Gotengo** | R: maser cannons. V: Freezing Maser (freezes kaiju and water). G: drill ram through rock. Flies and dives; the electrified hull shocks anything clinging to it |
| **Dimension Tide Uplink** | Hold use on a target for three seconds, release: a micro black hole that drags everything in and erases the ground. The target must be under open sky and not in water; Megaguirus or Meganula nearby jam the lock-on; long recharge |
| **Jet Jaguar** / **Jet Jaguar Controller** | Deploy your robot / tell your robots to grow or shrink |
| **Bladed Slicer** | Dropped by Modified Gigan. Throw it at what you're looking at: it homes in and loops back for a second pass |
| **Organizer G1** | Godzilla's regenerative cells: eating one gives strong Regeneration and Absorption |
| **Shisa Statue** | Place it near King Caesar's cliff and play a music disc in a jukebox beside it to wake him |
| **Minilla Egg** | Hatches only with lava, magma, fire or a campfire within two blocks below it. The hatching call draws Kamacuras |
| **Meganulon Egg Mass** | Buds off Meganulon while wet; feed it (via Meganula) to hatch Megaguirus |

| Recipe | Pattern |
|---|---|
| Kiryu | `TBT` / `NMN` / `T T`: Space Titanium, **Bones of the First Godzilla** (dropped by Showa/Heisei Godzilla), netherite blocks, Maser Tank |
| Kiryu Kai Upgrade Kit | `TDT` / `MRM` / `TIT`: Space Titanium, diamond block, Maser Tanks, redstone block, iron blocks |
| White Heron Power Cell (x2) | `IGI` / `RLR` / `IGI`: iron, glowstone dust, redstone blocks, lightning rod |
| Kiryu Maintenance Gantry | `ILI` / `RCR` / `III`: iron blocks, lightning rod, redstone blocks, White Heron Power Cell |
| Gotengo | `TDT` / `NSN` / `MEM`: Space Titanium, diamond block, netherite blocks, nether star, Maser Tanks, elytra |
| Dimension Tide Uplink | `EOE` / `TNT` / `RSR`: eyes of ender, observer, Space Titanium, nether star, redstone blocks, spyglass |
| D-03 Missile (x2) | ` I ` / `ITI` / `IFI`: iron, TNT, fire charge |
| Light Bomb (x2) | `G` / `T` / `P`: glowstone, TNT, paper |
| Supersonic Wave Oscillator | `NAN` / `RCR` / `I I`: note blocks, amethyst shard, redstone, comparator, iron |
| Jet Jaguar | `ROR` / `INI` / `I I`: redstone blocks, observer, iron blocks, netherite ingot |
| Jet Jaguar Controller | `  L` / `IRI` / `ICI`: lightning rod, iron, redstone, comparator |
| Electrode Plate (x2) | `CCC` / `IRI`: copper blocks, iron, redstone block |
| Yellow Juice | Shapeless: glass bottle, yellow dye, sweet berries, sugar |

### World

- **Sollgel Island** (warm oceans): the weather-control station and balloon tower, **Minilla's egg** on a bed of magma, the **Valley of Kumonga** (a web-choked pit) and a Kamacuras pack.
- **Letchi Island** (oceans): the Red Bamboo's base with its heavy-water plant and smokestack (Yellow Juice in the chest) and Ebirah patrolling offshore.
- **Monster Island** now also holds Gorosaurus, Baragon, Minilla, Varan and Manda.
- **The Godzilla Tower** (plains, meadows, savannas): World Children's Land. The tower's head holds the **Nebula M control room** (a transmitter controlling Gigan) and a mouth laser that fires at intruders. Gigan waits nearby.
- **Seatopia** (deep underground, anywhere): a cavern kingdom with an artificial sun, columns, statues, the Seatopian transmitter and Megalon's glowing spire. A shaft climbs to the surface.
- **The Azumi shrine** (savannas, jungles, beaches): King Caesar sleeps in the cliff. The chest holds the Shisa Statue and music discs.
- **GMK shrines**: Baragon's tunnel shrine (hills), Mothra's lake shrine (forests), King Ghidorah's ice cave (dark and snowy forests).
- **The Millennian UFO wreck** (deep oceans), **flooded Meganulon ruins** (swamps, plains), **Kiryu's hangar** (plains) with gantries and parts, and a landed **Xilien mothership** (plains, deserts, badlands) with the Xilien control core, Monster X and saucers.
- **New events**: Gigan and King Ghidorah from space, Megalon bursting out of the ground, Hedorah drifting in, a Xilien invasion (saucers plus controlled monsters, or Monster X), Titanosaurus with Mechagodzilla 2, the Millennian UFO, a Meganulon swarm, GMK Godzilla, and a Kamacuras pack.

## What's new in Phase 5: Reiwa and modern

### Shin Godzilla (2016)

| Monster | Side | What it does |
|---|---|---|
| **Shin Godzilla** | Invader | Comes ashore as the gilled, crawling **2nd form**, molts into the upright **3rd form**, then the 24-block **4th form**. Time on land and damage taken fill its evolution meter; when full it stops, steams and molts. The 4th form's breath goes black smoke → fire spray → focused purple beam. **Dorsal beams** shoot down anything in the air (players with elytra, flying mechs, missiles, drones) and a **tail beam** covers its back. Everything burns reactor energy: run it to zero and it goes **dormant**. Conventional weapons barely scratch it, and it adapts to whatever damage it takes most. It never hunts people; it shoots whatever hurts it |

**Operation Yashiori:** knock it down (train bombs, demolition charges) or catch it dormant, then pump coagulant into its mouth. At 100% it freezes solid, permanently. MOP II bunker busters dropped on its back break dorsal plates (fewer dorsal beams); breaking its tail tip silences the tail beam.

### Godzilla Minus One (2023) and Minus Zero (2026)

| Monster | Side | What it does |
|---|---|---|
| **Godzilla (Minus One)** | Invader | Regenerates about 2% of its health a second as long as nothing has hit its head for a few seconds. Big wounds leave scars that slow the healing; damage from the inside (explosives in the mouth, decompression, an overload) stops it for a long time. The **heat ray**: its plates click up from tail to head three times, lock, glow, then fire. The bolt sets off a blast, a ring-shaped pressure wave and a **black rain** that leaves fallout ash; the venting costs Godzilla health. Hit its head hard during the charge and it backfires. It fixates on whoever hurt it most recently |
| **Odo Island creature** | Invader | The 15 m creature of the island legend. Near a nuclear warhead it absorbs the radiation and mutates into Godzilla |
| **Godzilla (Minus Zero)** | Invader | Regenerated two years later: bigger (size in config, no official height yet) and heals faster |
| **Ghidorah (Minus Zero)** → **King Ghidorah** | Invader | Rides storms. Lightning wrapped in anti-gravity lifts blocks, people and kaiju into the air and smashes them down; hurricane wings. At half health, or after surviving a full day, it hides in its storm and grows two more heads (three gravity beams, snatch-and-drop). Always goes after Godzilla first |

**Operation Wada Tsumi:** while Godzilla is in deep water, attach two **Freon tanks** to sink him (heavy internal damage), then release a **balloon rig** to haul him up (more damage; he may claw free halfway). Finish with the **Shinden**: arm its charge and fly it into his head. If he's charging the heat ray, it turns inward. Eject first (G).

### The anime trilogy (2017-2018)

| Monster | Side | What it does |
|---|---|---|
| **Godzilla Filius** → **Godzilla Earth** | Titan | An electromagnetic shield blocks everything except blows from very close and hits to the dorsal fins. Every 45 s the shield organ recycles and drops for 3 s (watch for the crackle). Long-range charged-particle breath; Godzilla Earth (48 blocks tall by default, 60 with the true-scale config) also snipes anything flying high overhead. Pinned down, it vents heat that melts nanometal. Filius slowly grows into Godzilla Earth. Ignores people unless attacked |
| **Servum** | Invader | Packs of metal-skinned wyvern flyers and burrowing worms with root tentacles. Their EMP knocks out machines. Nanometal weapons do ten times the damage |
| **Mechagodzilla City core** | Human → Invader | Mechagodzilla's head inside its nanometal city. It never moves; it fights Godzilla with nanometal spikes, EMP harpoons, rail guns, a beam-deflecting particle cloud and liquid nanometal traps. While it lives, nanometal around it spreads; kill it and the nanometal dies. If a Vulture pilot is fully assimilated, the City turns on everyone |
| **Ghidorah, the Planet Eater** | Invader | Three golden necks reach through rifts in the sky and drain the life out of what they bite. While the Exif's **Garbetrium totem** stands, nothing can touch them. Break the totem and they become mortal, and fragile |

**EMP probes:** fire three into the fins while the shield is down. Filius explodes; Godzilla Earth is badly hurt.

### Godzilla Singular Point (2021)

| Monster | Side | What it does |
|---|---|---|
| **Godzilla Aquatilis → Amphibia → Terrestris → Ultima** | Invader | The sea form grows legs on land, the Amphibia cocoons itself, the Terrestris becomes **Ultima** under heavy fire. Ultima's thin breath cuts through anything and it fires a ring of breath around itself. From the Amphibia on it sheds **Red Dust**, and Ultima keeps growing while dust surrounds it |
| **Jet Jaguar (Singular Point)** | Human | A pilotable robot from the Otaki Factory. Feed it an Archetype crystal to rebuild it as Jet Jaguar β, then four more for the giant **Jet Jaguar PP**. R jet punch, V Archetype shield, G leap; carrying the **Diagonalizer Code**, Jet Jaguar PP releases it and ends every Singular Point Godzilla nearby, sacrificing itself |
| **Rodan (Singular Point)** | Invader | Screaming swarms led by a larger adult; their radio-wave cries jam machines. The small ones break down away from Red Dust |
| **Salunga** | Invader | Electrified punches that short out machines; eats Red Dust to heal |

**Red Dust** creeps outward a few generations from where it fell and weakens people standing in it. The **Orthogonal Diagonalizer** (uses Archetype crystals) crystallizes the dust nearby and disrupts Singular Point Godzilla.

### MonsterVerse, Skull Island and Godzilla Fest

| Monster | Side | What it does |
|---|---|---|
| **Titan X** (Co-Cai) | Titan | A huge bioluminescent sea Titan with Scarab swarms: tentacle grabs, a blinding glow pulse, fast healing in water. Apex's version has **neural implants**: break all three nodes on its head to free it. Take its **egg** and it hunts you; put the egg back in the water beside it and it calms for good |
| **Scarabs** | Titan | Harmless until hurt; their screech brings Titan X down on you |
| **Frost Vark** | Invader | Burrows and inhales heat: water freezes, fires and furnaces go out, people freeze stiff. Very weak to fire and explosions |
| **Ion Dragon** | Invader | Ionizing breath and trails, nacre spit. Tear a wing and it's grounded |
| **Kraken** (Skull Island, 2023) | Invader | Tentacle wraps, ink clouds, sinks boats |
| **Gigan Rex** and **Gigan Miles** | Invader | The crimson elite Gigan and his army of smaller Gigan soldiers |
| **Burning Millennium Godzilla** | Titan | Godzilla 2000 under 25% health melts down once a fight: stronger, a fire aura, a burning breath, but it costs him health every second |
| **SpaceGodzilla (MonsterVerse)** | Invader | A placeholder for *Godzilla x Kong: Supernova* (March 2027), whose design hasn't been revealed. It reuses the Heisei SpaceGodzilla's abilities and only appears in events if `reiwa.supernovaPlaceholder` is on |

### Tech

| Item | Use |
|---|---|
| **Coagulant Pump** + **Coagulant Canister** | Stand near a downed or dormant Shin Godzilla's head and use the pump: each canister adds 5% |
| **Train Bomb** | Three explosive trains run along the ground at the kaiju you're looking at and climb it |
| **Drone Swarm** | Six drones circle the nearest kaiju, baiting Shin Godzilla's dorsal beams |
| **Demolition Charge** | Flip the lever on top: the 7x7 column of building above comes down, stunning kaiju underneath |
| **MOP II Strike Designator** | Three bunker busters on what you're looking at. Long recharge |
| **Freon Tank**, **Balloon Rig** | Operation Wada Tsumi (above) |
| **Shinden** | R cannons, V arm/disarm the charge, G eject |
| **Radiation Buoy** | Points to the nearest kaiju within 256 blocks |
| **Storm Beacon** | Power it with redstone: it seeds a thunderstorm and every Ghidorah nearby heads for it |
| **Gravity Gland** | Dropped by Minus Zero Ghidorah: float up and drift down |
| **EMP Probe** | For Godzilla Earth and Filius (above) |
| **Vulture** | Nanometal powered suit: R rail cannons, V EMP harpoon. Near a living City core it slowly assimilates its pilot, unless you carry a **Houtua Scale** |
| **Nanometal Blade** | Ten times the damage against Servum |
| **Orthogonal Diagonalizer**, **Diagonalizer Code**, **Jet Jaguar (SP)** | Singular Point (above) |

| Recipe | Pattern |
|---|---|
| Coagulant Canister (x4) | Shapeless: contaminated blood (dropped by Shin Godzilla), prismarine crystals, slime ball, glass bottle |
| Coagulant Pump | ` P ` / `IBI` / `III`: piston, iron, bucket |
| Train Bomb | `TTT` / `MRM`: TNT, minecarts, powered rail |
| Drone Swarm (x2) | `IRI` / `PCP`: iron, redstone, phantom membrane, compass |
| Demolition Charge (x4) | `PTP` / `TRT` / `PTP`: paper, TNT, redstone |
| MOP II Strike Designator | `TST` / `RNR` / `T T`: Space Titanium, spyglass, redstone blocks, netherite ingot |
| Freon Tank (x2) | `ISI` / `SBS` / `ISI`: iron, snowballs, blue ice |
| Balloon Rig | `WWW` / `WSW` / ` C `: white wool, string, chain |
| Radiation Buoy | ` L ` / `RCR` / `IBI`: lightning rod, redstone, compass, iron, bucket |
| Shinden | `IEI` / `PTP` / ` I `: iron blocks, elytra, phantom membrane, TNT |
| Storm Beacon | `LCL` / `CRC` / `CCC`: lightning rods, copper blocks, compass |
| EMP Probe (x4) | ` R ` / `ILI` / ` I `: redstone, iron, lightning rod |
| Vulture | `NEN` / `NRN` / `N N`: nanometal ingots, elytra, redstone block |
| Nanometal Blade | Nanometal ingot, nanometal ingot, stick (vertical) |
| Orthogonal Diagonalizer | `ACA` / `RQR` / `III`: Archetype crystals, comparator, redstone blocks, amethyst shard, iron |
| Diagonalizer Code | Shapeless: book and quill, four Archetype crystals, nether star |
| Jet Jaguar (SP) | `ROR` / `IAI` / `I I`: redstone blocks, observer, iron blocks, Archetype crystal |

### World

- **Tokyo Ruins** (plains, savannas): a city grid of towers, some rigged with demolition charges, an elevated rail line and Operation Yashiori supply dumps.
- **Odo Island garrison** (beaches): airstrip, a hangar with a Shinden, bunkers, and the island's creature.
- **Wada Tsumi dock** (beaches, stony shores): destroyers, Freon depots and balloon rigs.
- **Matsumoto** (plains, meadows, cherry groves, forests): a 1949 castle town with a storm beacon on the keep.
- **Mechagodzilla City** (badlands, deserts): a nanometal crater with spike towers, a liquid-nanometal moat, Vulture hangars and the core on its central tower.
- **Houtua village** (jungles, dark forests): huts and moth totems in a metallic forest, with Servum nearby.
- **Exif observatory** (hills, meadows): a ruined gold-and-quartz temple around the Garbetrium totem. Use the totem to call the Planet Eater.
- **Otaki Factory** (plains, forests): Jet Jaguar's gantry and the Misakioku antenna.
- **SHIVA facility** (deep underground): a lab over a Red Dust pool with Salunga caged above it, and a shaft to the surface.
- **Apex lab** (beaches, plains): Titan X's stolen egg in a vault, and an implanted Titan X nearby.
- **Santa Soledad shrine** (beaches): an offering altar to "El Gran Dios del Mar", and Titan X offshore.
- **Outpost 88** (snowy biomes): Monarch's outpost over a sealed rift, a Frost Vark, and a self-destruct (demolition charges).
- **New events:** Shin Godzilla coming ashore, Minus One Godzilla's raid, a Ghidorah storm front (sometimes with Minus Zero Godzilla on its way), tremors with Servum (rarely Filius), the Singular Point Catastrophe, Titan X's migration, a Gigan Rex invasion, the Planet Eater, a Monarch Titan sighting (Frost Vark in the cold, Ion Dragon elsewhere), the Kraken, and the Supernova placeholder.

## Commands

`/kaiju event <godzilla|ghidorah|mechagodzilla|spacegodzilla|battra|destoroyah|monsterverse_godzilla|muto|scylla|behemoth|alpha_call|skullcrawlers|gigan|megalon|hedorah|xilien_invasion|titanosaurus|orga|meganulon|gmk_godzilla|kamacuras|shin_godzilla|ginza_raid|ghidorah_storm|earth_tremors|catastrophe|titan_x|gigan_invasion|planet_eater|monarch|kraken|supernova>` starts an event near you (needs operator permissions).

## Config

`config/reikogodzilla-common.toml`:

- `kaiju.scale`: size of every kaiju (1.0 = one fifth of film size).
- `kaiju.godzillaHostileToPlayers`, `kaiju.bossBars`, `kaiju.playerDamageMultiplier`, `kaiju.mothraGrowthTicks`
- `destruction.kaijuGriefing`, `beamBlockDamage`, `beamFire`, `maxBlocksPerTick`, `maxBlockResistance` (block breaking also respects the `mobGriefing` game rule)
- `events.enabled`, `firstDay`, `intervalTicks`, `chance`, `maxKaijuNearPlayer`
- `weapons.maserAutoFire`, `maserDamage`, `oxygenDestroyerKaijuDamage`
- `heisei.burningGodzilla`, `meltdownTicks`, `meltdownExplosion`, `frozenTicks`, `juniorGrowthTicks`, `destoroyahChance`, `biollanteGrowthStages`
- `monsterverse.alphaCall`, `kongGrowthTicks`, `apexTakeoverChance`, `iceAge`, `orcaRange`, `vortexDamage`
- `showa_millennium.controlRange`, `saucerControlRange`, `hedorahGrowth`, `minillaGrowthTicks`, `jetJaguarGiantTicks`, `kiryuRampageChance`, `dimensionTideRadius`, `dimensionTideCooldown`
- `reiwa.shinDormancyTicks`, `shinEvolutionRate`, `minusZeroScale`, `godzillaEarthScale`, `filiusGrowthTicks`, `nanometalRadius`, `redDustSpreads`, `ultimaMaxGrowth`, `mopCooldown`, `supernovaPlaceholder`

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
- Phase 4: `gigan`, `hedorah_aquatic`, `hedorah_land`, `hedorah_flying`, `hedorah_perfect`, `megalon`, `jet_jaguar`, `ebirah`, `kumonga`, `kamacuras`, `minilla`, `gorosaurus`, `manda`, `baragon`, `varan`, `king_caesar`, `titanosaurus`, `godzilla_millennium`, `godzilla_gmk`, `godzilla_kiryu_era`, `godzilla_final_wars`, `orga`, `millennian_ufo`, `megaguirus`, `meganulon`, `meganula`, `king_ghidorah_gmk`, `kiryu`, `monster_x`, `keizer_ghidorah`, `zilla`, `gotengo`, `xilien_saucer`
- Phase 5: `shin_godzilla_2`, `shin_godzilla_3`, `shin_godzilla`, `godzilla_odo`, `godzilla_minus_one`, `ghidorah_minus_zero`, `king_ghidorah_minus_zero`, `godzilla_earth`, `servum`, `servum_worm`, `vulture`, `mechagodzilla_city`, `planet_eater_ghidorah`, `godzilla_aquatilis`, `godzilla_amphibia`, `godzilla_terrestris`, `godzilla_ultima`, `jet_jaguar_pp`, `salunga`, `titan_x`, `scarab`, `frost_vark`, `ion_dragon`, `kraken`, `shinden`

Fire Rodan uses the `rodan` model with its own texture, `rodan_fire.png`. MonsterVerse Rodan and Mothra reuse the `rodan` and `mothra` models with the textures `rodan_mv.png` and `mothra_mv.png`. Phase 4 forms that share a model with their own texture: `gigan_final_wars`, `hedorah_final_wars` (on `hedorah_perfect`), `ebirah_final_wars`, `kumonga_final_wars`, `kamacuras_final_wars`, `manda_final_wars`, `baragon_gmk`, `king_caesar_final_wars`, `mechagodzilla_2` (on `mechagodzilla`), `mothra_gmk` (on `mothra`), `king_ghidorah_dragon_king` (on `king_ghidorah_gmk`) and `kiryu_kai` (on `kiryu`). Phase 5: `godzilla_minus_zero` (on `godzilla_minus_one`), `godzilla_filius` (on `godzilla_earth`), `rodan_sp` (on `rodan`), `gigan_rex` and `gigan_miles` (on `gigan`) and `space_godzilla_mv` (on `space_godzilla`). Burning Godzilla tints `godzilla_heisei` orange.

**Bones:** a bone named `head` turns to follow where the kaiju is looking. A few bones are shown or hidden by the game: `axe` and `glove` on Kong (when he has them), `crystal` on the Skar King (until it shatters), `head_0`, `head_1`, `head_2` on MonsterVerse Ghidorah (while a head is severed), `hook_left`/`hook_right` and `saw_left`/`saw_right` on Gigan (hammer hands, swapped for chainsaws once he's Modified), and `zero_cannon`, `drill` and `triple_maser` on Kiryu (the cannon for Kiryu, the other two for Kiryu Kai), and `implants` on Titan X (shown while Apex's implants are in). Everything else is up to you.

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
| `dormant` | Methuselah asleep as a mountain, Shimo in chains, King Caesar asleep in his cliff, Shin Godzilla dormant or knocked down, Singular Point Godzilla's cocoon |
| `swim` | Tiamat in water |
| `saw` | Holds that grind or crush: Gigan's buzzsaw, Megalon's drills (some holds use `grab` instead) |
| `dig`, `burrow` | Digging in and tunnelling underground (Megalon, Baragon, Kumonga) |

Beam start points (mouths, Ghidorah's three heads, Mechagodzilla's eyes, weapon mounts on the mechs) and extra hitboxes are set in each kaiju's class (`beamLocalOffset` and `createParts`), measured in blocks at scale 1 with +z forward. Adjust them if your model's proportions differ.

## Sounds

Sounds are currently re-pitched vanilla sounds. To use your own, put `.ogg` files in `src/main/resources/assets/reikogodzilla/sounds/` and point the entries in `assets/reikogodzilla/sounds.json` at them (for example `"reikogodzilla:godzilla_roar"`).

## Building

```
./gradlew build
```

The mod jar ends up in `build/libs/`. Use `./gradlew runClient` to launch a test game.

Mechs use a small client-side mixin (`CameraMixin`) for the over-the-shoulder pilot camera.
